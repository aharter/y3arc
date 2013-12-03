package de.andreas.harter.yaaarc.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import de.andreas.harter.yaaarc.model.AminoAcid;
import de.andreas.harter.yaaarc.model.CytosolBasePair;
import de.andreas.harter.yaaarc.model.MitochondriaBasePair;

public class DbAccess {

	private static final String DATABASE_URL = "jdbc:sqlite::resource:yaaarc.db";
	private static final String PROBABILITY_COLUMN = "probability";
	private static final String AMINO_ACID_ID_COLUMN = "aminoAcid_id";

	private static DbAccess dbAccess = null;

	private ConnectionSource connectionSource;

	private Dao<AminoAcid, String> aminoAcidDao;
	private Dao<MitochondriaBasePair, String> mitochondriaBasePairDao;
	private Dao<CytosolBasePair, String> cytosolBasePairDao;

	private DbAccess() {
		try {
			connectionSource = new JdbcConnectionSource(DATABASE_URL);
			aminoAcidDao = DaoManager.createDao(connectionSource, AminoAcid.class);
			mitochondriaBasePairDao = DaoManager.createDao(connectionSource, MitochondriaBasePair.class);
			cytosolBasePairDao = DaoManager.createDao(connectionSource, CytosolBasePair.class);
		} catch (SQLException e) {
			e.printStackTrace();
			connectionSource = null;
			aminoAcidDao = null;
			mitochondriaBasePairDao = null;
			cytosolBasePairDao = null;
		}
	}

	public static DbAccess getInstance() {
		if (dbAccess == null) {
			dbAccess = new DbAccess();
		}
		return dbAccess;
	}

	public void importDataMitochondriaBasePair(Map<AminoAcid, List<MitochondriaBasePair>> mapping) {
		try {

			setupTables(connectionSource);
			importAminoAcid(mapping.keySet());
			importMitochondriaBasePair(mapping);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void importDataCytosolBasePair(Map<AminoAcid, List<CytosolBasePair>> mapping) {
		try {

			setupTables(connectionSource);
			importAminoAcid(mapping.keySet());
			importCytosolBasePair(mapping);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<MitochondriaBasePair> queryMitochondriaBasePairsForAminoAcid(AminoAcid aminoAcid) {
		QueryBuilder<MitochondriaBasePair, String> qb = mitochondriaBasePairDao.queryBuilder();
		qb = qb.orderBy(PROBABILITY_COLUMN, false);
		Where<MitochondriaBasePair, String> whereClause = qb.where();
		List<MitochondriaBasePair> mitochondriaBasePairs = null;
		try {
			mitochondriaBasePairs = whereClause.eq(AMINO_ACID_ID_COLUMN, aminoAcid.getIdentifier()).query();
		} catch (SQLException e) {
			mitochondriaBasePairs = null;
		}

		return mitochondriaBasePairs;
	}

	public List<CytosolBasePair> queryCytosolBasePairForAminoAcid(AminoAcid aminoAcid) {
		QueryBuilder<CytosolBasePair, String> qb = cytosolBasePairDao.queryBuilder();
		qb = qb.orderBy(PROBABILITY_COLUMN, false);
		Where<CytosolBasePair, String> whereClause = qb.where();
		List<CytosolBasePair> mitochondriaBasePairs = null;
		try {
			mitochondriaBasePairs = whereClause.eq(AMINO_ACID_ID_COLUMN, aminoAcid.getIdentifier()).query();
		} catch (SQLException e) {
			mitochondriaBasePairs = null;
		}

		return mitochondriaBasePairs;
	}

	public List<MitochondriaBasePair> queryMitochondriaBasePairsForCytosolBasePair(CytosolBasePair cytosolBasePair) {
		if (cytosolBasePair.getAminoAcid() == null || cytosolBasePair.getAminoAcid().getIdentifier().equals("")) {
			try {
				cytosolBasePair = cytosolBasePairDao.queryForId(cytosolBasePair.getCoding());
			} catch (SQLException e) {
				// TODO log this stuff
				return new ArrayList<MitochondriaBasePair>();
			}
		}

		if (cytosolBasePair == null) {
			return new ArrayList<MitochondriaBasePair>();
		}

		return queryMitochondriaBasePairsForAminoAcid(cytosolBasePair.getAminoAcid());
	}

	private void importAminoAcid(Set<AminoAcid> aminoAcids) throws SQLException {
		for (AminoAcid aminoAcid : aminoAcids) {
			aminoAcidDao.createOrUpdate(aminoAcid);
		}
	}

	private void importMitochondriaBasePair(Map<AminoAcid, List<MitochondriaBasePair>> mapping) throws SQLException {
		for (List<MitochondriaBasePair> mitochondriaBasePairs : mapping.values()) {
			for (MitochondriaBasePair mitochondriaBasePair : mitochondriaBasePairs) {
				mitochondriaBasePairDao.createOrUpdate(mitochondriaBasePair);
			}
		}
	}

	private void importCytosolBasePair(Map<AminoAcid, List<CytosolBasePair>> mapping) throws SQLException {
		for (List<CytosolBasePair> cytosolBasePairs : mapping.values()) {
			for (CytosolBasePair cytosolBasePair : cytosolBasePairs) {
				cytosolBasePairDao.createOrUpdate(cytosolBasePair);
			}
		}
	}

	private void setupTables(ConnectionSource connectionSource) throws SQLException {
		TableUtils.createTableIfNotExists(connectionSource, AminoAcid.class);
		TableUtils.createTableIfNotExists(connectionSource, MitochondriaBasePair.class);
		TableUtils.createTableIfNotExists(connectionSource, CytosolBasePair.class);
	}

}
