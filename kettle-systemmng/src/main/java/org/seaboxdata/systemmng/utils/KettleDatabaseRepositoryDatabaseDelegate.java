package org.seaboxdata.systemmng.utils;


//package org.pentaho.di.repository.kdr.delegates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleDependencyException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.delegates.KettleDatabaseRepositoryBaseDelegate;
import org.seaboxdata.systemmng.entity.UserEntity;

public class KettleDatabaseRepositoryDatabaseDelegate extends KettleDatabaseRepositoryBaseDelegate {
    private static final Class<?> PKG = DatabaseMeta.class;

    public KettleDatabaseRepositoryDatabaseDelegate(KettleDatabaseRepository repository) {
        super(repository);
    }

    public synchronized ObjectId getDatabaseID(String name) throws KettleException {
        return this.repository.connectionDelegate.getIDWithValue(this.quoteTable("R_DATABASE"), this.quote("ID_DATABASE"), this.quote("NAME"), name);
    }

    public synchronized String getDatabaseTypeCode(ObjectId id_database_type) throws KettleException {
        return this.repository.connectionDelegate.getStringWithID(this.quoteTable("R_DATABASE_TYPE"), this.quote("ID_DATABASE_TYPE"), id_database_type, this.quote("CODE"));
    }

    public synchronized String getDatabaseConTypeCode(ObjectId id_database_contype) throws KettleException {
        return this.repository.connectionDelegate.getStringWithID(this.quoteTable("R_DATABASE_CONTYPE"), this.quote("ID_DATABASE_CONTYPE"), id_database_contype, this.quote("CODE"));
    }

    public RowMetaAndData getDatabase(ObjectId id_database) throws KettleException {
        return this.repository.connectionDelegate.getOneRow(this.quoteTable("R_DATABASE"), this.quote("ID_DATABASE"), id_database);
    }

    public RowMetaAndData getDatabaseAttribute(ObjectId id_database_attribute) throws KettleException {
        return this.repository.connectionDelegate.getOneRow(this.quoteTable("R_DATABASE_ATTRIBUTE"), this.quote("ID_DATABASE_ATTRIBUTE"), id_database_attribute);
    }

    public Collection<RowMetaAndData> getDatabaseAttributes() throws KettleDatabaseException, KettleValueException {
        List<RowMetaAndData> attrs = new ArrayList();
        List<Object[]> rows = this.repository.connectionDelegate.getRows("SELECT * FROM " + this.quoteTable("R_DATABASE_ATTRIBUTE"), 0);
        Iterator var3 = rows.iterator();

        while(var3.hasNext()) {
            Object[] row = (Object[])var3.next();
            RowMetaAndData rowWithMeta = new RowMetaAndData(this.repository.connectionDelegate.getReturnRowMeta(), row);
            long id = rowWithMeta.getInteger(this.quote("ID_DATABASE_ATTRIBUTE"), 0L);
            if (id > 0L) {
                attrs.add(rowWithMeta);
            }
        }

        return attrs;
    }

    public DatabaseMeta loadDatabaseMeta(ObjectId id_database) throws KettleException {
        DatabaseMeta databaseMeta = new DatabaseMeta();

        try {
            RowMetaAndData r = this.getDatabase(id_database);
            if (r != null) {
                ObjectId id_database_type = new LongObjectId(r.getInteger("ID_DATABASE_TYPE", 0L));
                String dbTypeDesc = this.getDatabaseTypeCode(id_database_type);
                if (dbTypeDesc != null) {
                    databaseMeta.setDatabaseInterface(DatabaseMeta.getDatabaseInterface(dbTypeDesc));
                    databaseMeta.setAttributes(new Properties());
                }

                databaseMeta.setObjectId(id_database);
                databaseMeta.setName(r.getString("NAME", ""));
                ObjectId id_database_contype = new LongObjectId(r.getInteger("ID_DATABASE_CONTYPE", 0L));
                databaseMeta.setAccessType(DatabaseMeta.getAccessType(this.getDatabaseConTypeCode(id_database_contype)));
                databaseMeta.setHostname(r.getString("HOST_NAME", ""));
                databaseMeta.setDBName(r.getString("DATABASE_NAME", ""));
                databaseMeta.setDBPort(r.getString("PORT", ""));
                databaseMeta.setUsername(r.getString("USERNAME", ""));
                databaseMeta.setPassword(Encr.decryptPasswordOptionallyEncrypted(r.getString("PASSWORD", "")));
                databaseMeta.setServername(r.getString("SERVERNAME", ""));
                databaseMeta.setDataTablespace(r.getString("DATA_TBS", ""));
                databaseMeta.setIndexTablespace(r.getString("INDEX_TBS", ""));
                Collection<RowMetaAndData> attrs = this.repository.connectionDelegate.getDatabaseAttributes(id_database);
                Iterator var8 = attrs.iterator();

                while(var8.hasNext()) {
                    RowMetaAndData row = (RowMetaAndData)var8.next();
                    String code = row.getString("CODE", "");
                    String attribute = row.getString("VALUE_STR", "");
                    databaseMeta.getAttributes().put(code, Const.NVL(attribute, ""));
                }
            }

            return databaseMeta;
        } catch (KettleDatabaseException var12) {
            throw new KettleException("Error loading database connection from repository (id_database=" + id_database + ")", var12);
        }
    }

    public void saveDatabaseMeta(DatabaseMeta databaseMeta) throws KettleException {
        try {
            if (databaseMeta.getObjectId() == null) {
                databaseMeta.setObjectId(this.getDatabaseID(databaseMeta.getName()));
            }

            if (databaseMeta.getObjectId() == null) {
                databaseMeta.setObjectId(this.insertDatabase(databaseMeta.getName(), databaseMeta.getPluginId(), DatabaseMeta.getAccessTypeDesc(databaseMeta.getAccessType()), databaseMeta.getHostname(), databaseMeta.getDatabaseName(), databaseMeta.getDatabasePortNumberString(), databaseMeta.getUsername(), databaseMeta.getPassword(), databaseMeta.getServername(), databaseMeta.getDataTablespace(), databaseMeta.getIndexTablespace()));
            } else {
                this.updateDatabase(databaseMeta.getObjectId(), databaseMeta.getName(), databaseMeta.getPluginId(), DatabaseMeta.getAccessTypeDesc(databaseMeta.getAccessType()), databaseMeta.getHostname(), databaseMeta.getDatabaseName(), databaseMeta.getDatabasePortNumberString(), databaseMeta.getUsername(), databaseMeta.getPassword(), databaseMeta.getServername(), databaseMeta.getDataTablespace(), databaseMeta.getIndexTablespace());
            }

            this.delDatabaseAttributes(databaseMeta.getObjectId());
            this.insertDatabaseAttributes(databaseMeta.getObjectId(), databaseMeta.getAttributes());
        } catch (KettleDatabaseException var3) {
            throw new KettleException("Error saving database connection or one of its attributes to the repository.", var3);
        }
    }

    public synchronized ObjectId insertDatabase(String name, String type, String access, String host, String dbname, String port, String user, String pass, String servername, String data_tablespace, String index_tablespace) throws KettleException {
        ObjectId id = this.repository.connectionDelegate.getNextDatabaseID();
        ObjectId id_database_type = this.getDatabaseTypeID(type);
        if (id_database_type == null) {
            id_database_type = this.repository.connectionDelegate.getNextDatabaseTypeID();
            String tablename = "R_DATABASE_TYPE";
            RowMetaInterface tableMeta = new RowMeta();
            tableMeta.addValueMeta(new ValueMetaInteger("ID_DATABASE_TYPE", 5, 0));
            tableMeta.addValueMeta(new ValueMetaString("CODE", 255, 0));
            tableMeta.addValueMeta(new ValueMetaString("DESCRIPTION", 2000000, 0));
            this.repository.connectionDelegate.getDatabase().prepareInsert(tableMeta, tablename);
            Object[] tableData = new Object[3];
            int tableIndex = 0;
            int var20 = tableIndex + 1;
            tableData[tableIndex] = (new LongObjectId(id_database_type)).longValue();
            tableData[var20++] = type;
            tableData[var20++] = type;
            this.repository.connectionDelegate.getDatabase().setValuesInsert(tableMeta, tableData);
            this.repository.connectionDelegate.getDatabase().insertRow();
            this.repository.connectionDelegate.getDatabase().closeInsert();
        }



        ObjectId id_database_contype = this.getDatabaseConTypeID(access);
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMetaInteger("ID_DATABASE"), id);
        table.addValue(new ValueMetaString("NAME"), name);
        table.addValue(new ValueMetaInteger("ID_DATABASE_TYPE"), id_database_type);
        table.addValue(new ValueMetaInteger("ID_DATABASE_CONTYPE"), id_database_contype);
        table.addValue(new ValueMetaString("HOST_NAME"), host);
        table.addValue(new ValueMetaString("DATABASE_NAME"), dbname);
        table.addValue(new ValueMetaInteger("PORT"), Const.toLong(port, -1L));
        table.addValue(new ValueMetaString("USERNAME"), user);
        table.addValue(new ValueMetaString("PASSWORD"), Encr.encryptPasswordIfNotUsingVariables(pass));
        table.addValue(new ValueMetaString("SERVERNAME"), servername);
        table.addValue(new ValueMetaString("DATA_TBS"), data_tablespace);
        table.addValue(new ValueMetaString("INDEX_TBS"), index_tablespace);

        table.addValue(new ValueMetaString("LOGIN_USER"), index_tablespace);
        this.repository.connectionDelegate.getDatabase().prepareInsert(table.getRowMeta(), "R_DATABASE");
        this.repository.connectionDelegate.getDatabase().setValuesInsert(table);
        this.repository.connectionDelegate.getDatabase().insertRow();
        this.repository.connectionDelegate.getDatabase().closeInsert();
        return id;
    }

    public synchronized void updateDatabase(ObjectId id_database, String name, String type, String access, String host, String dbname, String port, String user, String pass, String servername, String data_tablespace, String index_tablespace) throws KettleException {
        ObjectId id_database_type = this.getDatabaseTypeID(type);
        ObjectId id_database_contype = this.getDatabaseConTypeID(access);
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMetaString("NAME"), name);
        table.addValue(new ValueMetaInteger("ID_DATABASE_TYPE"), id_database_type);
        table.addValue(new ValueMetaInteger("ID_DATABASE_CONTYPE"), id_database_contype);
        table.addValue(new ValueMetaString("HOST_NAME"), host);
        table.addValue(new ValueMetaString("DATABASE_NAME"), dbname);
        table.addValue(new ValueMetaInteger("PORT"), Const.toLong(port, -1L));
        table.addValue(new ValueMetaString("USERNAME"), user);
        table.addValue(new ValueMetaString("PASSWORD"), Encr.encryptPasswordIfNotUsingVariables(pass));
        table.addValue(new ValueMetaString("SERVERNAME"), servername);
        table.addValue(new ValueMetaString("DATA_TBS"), data_tablespace);
        table.addValue(new ValueMetaString("INDEX_TBS"), index_tablespace);
        this.repository.connectionDelegate.updateTableRow("R_DATABASE", "ID_DATABASE", table, id_database);
    }

    public synchronized ObjectId getDatabaseTypeID(String code) throws KettleException {
        return this.repository.connectionDelegate.getIDWithValue(this.quoteTable("R_DATABASE_TYPE"), this.quote("ID_DATABASE_TYPE"), this.quote("CODE"), code);
    }

    public synchronized ObjectId getDatabaseConTypeID(String code) throws KettleException {
        return this.repository.connectionDelegate.getIDWithValue(this.quoteTable("R_DATABASE_CONTYPE"), this.quote("ID_DATABASE_CONTYPE"), this.quote("CODE"), code);
    }

    public void deleteDatabaseMeta(String databaseName) throws KettleException {
        this.repository.getSecurityProvider().validateAction(new RepositoryOperation[]{RepositoryOperation.DELETE_DATABASE});

        try {
            ObjectId id_database = this.getDatabaseID(databaseName);
            this.delDatabase(id_database);
        } catch (KettleException var3) {
            throw new KettleException(BaseMessages.getString(PKG, "KettleDatabaseRepository.Exception.ErrorDeletingConnection.Message", new String[]{databaseName}), var3);
        }
    }

    public synchronized void delDatabase(ObjectId id_database) throws KettleException {
        this.repository.getSecurityProvider().validateAction(new RepositoryOperation[]{RepositoryOperation.DELETE_DATABASE});
        String[] transList = this.repository.getTransformationsUsingDatabase(id_database);
        String[] jobList = this.repository.getJobsUsingDatabase(id_database);
        if (jobList.length == 0 && transList.length == 0) {
            this.repository.connectionDelegate.performDelete("DELETE FROM " + this.quoteTable("R_DATABASE") + " WHERE " + this.quote("ID_DATABASE") + " = ? ", new ObjectId[]{id_database});
        } else {
            String message = " Database used by the following " + Const.CR;
            String[] var5;
            int var6;
            int var7;
            String trans;
            if (jobList.length > 0) {
                message = "jobs :" + Const.CR;
                var5 = jobList;
                var6 = jobList.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    trans = var5[var7];
                    message = message + "\t " + trans + Const.CR;
                }
            }

            message = message + "transformations:" + Const.CR;
            var5 = transList;
            var6 = transList.length;

            for(var7 = 0; var7 < var6; ++var7) {
                trans = var5[var7];
                message = message + "\t " + trans + Const.CR;
            }

            KettleDependencyException e = new KettleDependencyException(message);
            throw new KettleDependencyException("This database is still in use by " + jobList.length + " jobs and " + transList.length + " transformations references", e);
        }
    }

    public synchronized void delDatabaseAttributes(ObjectId id_database) throws KettleException {
        this.repository.connectionDelegate.performDelete("DELETE FROM " + this.quoteTable("R_DATABASE_ATTRIBUTE") + " WHERE " + this.quote("ID_DATABASE") + " = ? ", new ObjectId[]{id_database});
    }

    public synchronized int getNrDatabases() throws KettleException {
        int retval = 0;
        String sql = "SELECT COUNT(*) FROM " + this.quoteTable("R_DATABASE");
        RowMetaAndData r = this.repository.connectionDelegate.getOneRow(sql);
        if (r != null) {
            retval = (int)r.getInteger(0, 0L);
        }

        return retval;
    }

    public synchronized int getNrDatabases(ObjectId id_transformation) throws KettleException {
        int retval = 0;
        RowMetaAndData transIdRow = this.repository.connectionDelegate.getParameterMetaData(new ObjectId[]{id_transformation});
        String sql = "SELECT COUNT(*) FROM " + this.quoteTable("R_STEP_DATABASE") + " WHERE " + this.quote("ID_TRANSFORMATION") + " = ? ";
        RowMetaAndData r = this.repository.connectionDelegate.getOneRow(sql, transIdRow.getRowMeta(), transIdRow.getData());
        if (r != null) {
            retval = (int)r.getInteger(0, 0L);
        }

        return retval;
    }

    public synchronized int getNrDatabaseAttributes(ObjectId id_database) throws KettleException {
        int retval = 0;
        String sql = "SELECT COUNT(*) FROM " + this.quoteTable("R_DATABASE_ATTRIBUTE") + " WHERE " + this.quote("ID_DATABASE") + " = " + id_database;
        RowMetaAndData r = this.repository.connectionDelegate.getOneRow(sql);
        if (r != null) {
            retval = (int)r.getInteger(0, 0L);
        }

        return retval;
    }

    private RowMetaAndData createAttributeRow(ObjectId idDatabase, String code, String strValue) throws KettleException {
        ObjectId id = this.repository.connectionDelegate.getNextDatabaseAttributeID();
        RowMetaAndData table = new RowMetaAndData();
        table.addValue(new ValueMetaInteger("ID_DATABASE_ATTRIBUTE"), id);
        table.addValue(new ValueMetaInteger("ID_DATABASE"), idDatabase);
        table.addValue(new ValueMetaString("CODE"), code);
        table.addValue(new ValueMetaString("VALUE_STR"), strValue);
        return table;
    }

    private void insertDatabaseAttributes(ObjectId idDatabase, Properties properties) throws KettleException {
        if (!properties.isEmpty()) {
            Database db = this.repository.connectionDelegate.getDatabase();
            boolean firstAttribute = true;
            Enumeration keys = properties.keys();

            while(keys.hasMoreElements()) {
                String code = (String)keys.nextElement();
                String attribute = (String)properties.get(code);
                RowMetaAndData attributeData = this.createAttributeRow(idDatabase, code, attribute);
                if (firstAttribute) {
                    db.prepareInsert(attributeData.getRowMeta(), "R_DATABASE_ATTRIBUTE");
                    firstAttribute = false;
                }

                db.setValuesInsert(attributeData);
                db.insertRow(db.getPrepStatementInsert(), true, false);
            }

            db.executeAndClearBatch(db.getPrepStatementInsert());
            db.closeInsert();
        }
    }
}
