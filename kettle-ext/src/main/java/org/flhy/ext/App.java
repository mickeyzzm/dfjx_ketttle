package org.flhy.ext;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.flhy.ext.core.PropsUI;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.repository.LongObjectId;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepository;
import org.pentaho.di.repository.kdr.KettleDatabaseRepositoryMeta;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class App implements ApplicationContextAware {

	private static App app;
	public static KettleDatabaseRepositoryMeta meta;

	@Value("${jdbc.type}")
	private String jdbcType;
	@Value("${jdbc.hostName}")
	private String hostName;
	@Value("${jdbc.port}")
	private String port;
	@Value("${jdbc.dbName}")
	private String dbName;
	@Value("${jdbc.username}")
	private String username;
	@Value("${jdbc.password}")
	private String password;
	@Value("${jdbc.parms}")
	private String jdbcParms;

	private LogChannelInterface log;
	private TransExecutionConfiguration transExecutionConfiguration;
	private TransExecutionConfiguration transPreviewExecutionConfiguration;
	private TransExecutionConfiguration transDebugExecutionConfiguration;
	private JobExecutionConfiguration jobExecutionConfiguration;
	private PropsUI props;

	private App() {
//		LanguageChoice.getInstance().setDefaultLocale(Locale.US);
		props = PropsUI.getInstance();
		log = new LogChannel(PropsUI.getAppName());
		loadSettings();

		transExecutionConfiguration = new TransExecutionConfiguration();
		transExecutionConfiguration.setGatheringMetrics(true);
		transPreviewExecutionConfiguration = new TransExecutionConfiguration();
		transPreviewExecutionConfiguration.setGatheringMetrics(true);
		transDebugExecutionConfiguration = new TransExecutionConfiguration();
		transDebugExecutionConfiguration.setGatheringMetrics(true);

		jobExecutionConfiguration = new JobExecutionConfiguration();

		variables = new RowMetaAndData(new RowMeta());
	}

	public void loadSettings() {
		LogLevel logLevel = LogLevel.getLogLevelForCode(props.getLogLevel());
		DefaultLogLevel.setLogLevel(logLevel);
		log.setLogLevel(logLevel);
		KettleLogStore.getAppender().setMaxNrLines(props.getMaxNrLinesInLog());

		// transMeta.setMaxUndo(props.getMaxUndo());
		DBCache.getInstance().setActive(props.useDBCache());
	}

	public static App getInstance() {
		if (app == null) {
			app = new App();
		}
		return app;
	}

	private Repository repository;

	public Repository getRepository() {
		return repository;
	}

	private Repository defaultRepository;

	public Repository getDefaultRepository() {
		return this.defaultRepository;
	}

	public void selectRepository(Repository repo) {
		if (repository != null) {
			repository.disconnect();
		}
		repository = repo;
	}

	private DelegatingMetaStore metaStore;

	public DelegatingMetaStore getMetaStore() {
		return metaStore;
	}

	public LogChannelInterface getLog() {
		return log;
	}

	private RowMetaAndData variables = null;
	private ArrayList<String> arguments = new ArrayList<String>();

	public String[] getArguments() {
		return arguments.toArray(new String[arguments.size()]);
	}

	public JobExecutionConfiguration getJobExecutionConfiguration() {
		return jobExecutionConfiguration;
	}

	public TransExecutionConfiguration getTransDebugExecutionConfiguration() {
		return transDebugExecutionConfiguration;
	}

	public TransExecutionConfiguration getTransPreviewExecutionConfiguration() {
		return transPreviewExecutionConfiguration;
	}

	public TransExecutionConfiguration getTransExecutionConfiguration() {
		return transExecutionConfiguration;
	}

	public RowMetaAndData getVariables() {
		return variables;
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		KettleDatabaseRepository repository = new KettleDatabaseRepository();
		try {

			String prefix = this.hostName + "_" + this.port + "_" + dbName;
			DatabaseMeta dbMeta = new DatabaseMeta();
			dbMeta.setName(prefix + "_Database");
			dbMeta.setDBName(this.dbName);
			dbMeta.setDatabaseType(this.jdbcType);
			dbMeta.setAccessType(0);
			dbMeta.setHostname(this.hostName);
			dbMeta.setServername(this.hostName);
			dbMeta.setDBPort(this.port);
			dbMeta.setUsername(this.username);
			dbMeta.setPassword(this.password);
			ObjectId objectId = new LongObjectId(100);
			dbMeta.setObjectId(objectId);
			dbMeta.setShared(true);

			if (StringUtils.isNotBlank(jdbcParms)) {
				String[] jdbcParmArray = jdbcParms.split("&");
				if (jdbcParmArray != null && jdbcParmArray.length > 0) {
					for (String parm : jdbcParmArray) {
						String[] kv = parm.split("=");
						if (kv != null && kv.length == 2) {
							dbMeta.addExtraOption(dbMeta.getPluginId(), kv[0], kv[1]);
						}
					}
				}
			}

			dbMeta.setUsingConnectionPool(true);

			meta = new KettleDatabaseRepositoryMeta();
			meta.setName(prefix + "_DatabaseRepository");
			meta.setId("KettleDatabaseRepository");
			meta.setConnection(dbMeta);
			meta.setDescription(prefix + "_DatabaseRepository");
			repository.init(meta);
			repository.connect(username, this.password);
			this.repository = repository;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
