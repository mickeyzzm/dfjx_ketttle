package org.flhy.platform.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.flhy.ext.core.PropsUI;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.seaboxdata.systemmng.util.TaskUtil.CarteTaskManager;

import com.google.common.io.Resources;

public class SystemLoadListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent context) {
		
	}

	@Override
	public void contextInitialized(ServletContextEvent context) {
		try {
			
			String kettleHome=Resources.getResource("").getPath();
			
			System.setProperty("KETTLE_HOME", kettleHome);
			
			KettleEnvironment.init();
			
			CarteTaskManager.startThread(1);
			PropsUI.init( "重庆市政务数据资源接入系统", Props.TYPE_PROPERTIES_KITCHEN );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
