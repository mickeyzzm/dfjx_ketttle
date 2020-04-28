package org.flhy.ext.utils;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.flhy.ext.App;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.AreaOwner;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.SwingGC;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.JobPainter;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPainter;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.version.BuildVersion;

public class RepositoryUtils {

	public static JobMeta loadJobByPath(String path) throws KettleException {
		JobMeta jobMeta = null;

		if (StringUtils.isBlank(path)) {
			return jobMeta;
		}

		String dir = path.substring(0, path.lastIndexOf("/"));
		String name = path.substring(path.lastIndexOf("/") + 1);

		try {
			Repository repository = getRepository();
			if (repository != null) {
				RepositoryDirectoryInterface directory = repository.findDirectory(dir);
				if (directory == null) {
					directory = repository.getUserHomeDirectory();
				}
				jobMeta = repository.loadJob(name, directory, null, null);
				if (jobMeta != null) {
					jobMeta.setJobversion(BuildVersion.getInstance().getVersion());
				}
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return jobMeta;
	}

	public static JobMeta loadJobById(String jobId) {
		JobMeta jobMeta = null;
		if (StringUtils.isBlank(jobId)) {
			return jobMeta;
		}
		try {
			Repository repository = getRepository();
			StringObjectId objId = new StringObjectId(jobId);
			if (repository != null) {
				jobMeta = repository.loadJob(objId, null);
				if (jobMeta != null) {
					jobMeta.setJobversion(BuildVersion.getInstance().getVersion());
				}
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return jobMeta;
	}

	public static TransMeta loadTransByPath(String path) throws KettleException {
		TransMeta transMeta = null;
		if (StringUtils.isBlank(path)) {
			return transMeta;
		}

		String dir = path.substring(0, path.lastIndexOf("/"));
		String name = path.substring(path.lastIndexOf("/") + 1);

		try {
			Repository repository = getRepository();
			if (repository != null) {
				RepositoryDirectoryInterface directory = repository.findDirectory(dir);
				if (directory == null) {
					directory = repository.getUserHomeDirectory();
				}
				transMeta = repository.loadTransformation(name, directory, null, true, null);
				if (transMeta != null) {
					transMeta.setTransversion(BuildVersion.getInstance().getVersion());
				}
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return transMeta;
	}

	public static TransMeta loadTransMeta(String transId) {
		TransMeta transMeta = null;
		if (StringUtils.isBlank(transId)) {
			return transMeta;
		}
		try {
			Repository repository = getRepository();
			StringObjectId objId = new StringObjectId(transId);
			if (repository != null) {
				transMeta = repository.loadTransformation(objId, null);
				if (transMeta != null) {
					transMeta.setTransversion(BuildVersion.getInstance().getVersion());
				}
			}
		} catch (KettleException e) {
			e.printStackTrace();
		}
		return transMeta;
	}

	public static RepositoryDirectoryInterface loadDirectory(String dir) throws KettleException {
		Repository repository = getRepository();
		RepositoryDirectoryInterface directory = repository.findDirectory(dir);
		if (directory == null) {
			directory = repository.getUserHomeDirectory();
		}
		return directory;
	}

	/**
	 * 生成任务图片
	 * 
	 * @param jobMeta
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage generateJobImage(JobMeta jobMeta) {
		BufferedImage image = null;
		try {
			float magnification = 1.0f;
			Point maximum = jobMeta.getMaximum();
			maximum.multiply(magnification);
			SwingGC gc = new SwingGC(null, maximum, 32, 0, 0);
			JobPainter jobPainter = new JobPainter(gc, jobMeta, maximum, null, null, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<JobEntryCopy>(), 32, 1, 0, 0, true,
					"Arial", 10);
			jobPainter.setMagnification(magnification);
			jobPainter.drawJob();
			image = (BufferedImage) gc.getImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	/**
	 * 生成转换图片
	 * 
	 * @param transMeta
	 * @return
	 * @throws Exception
	 */
	public static BufferedImage generateTransformationImage(TransMeta transMeta) {
		BufferedImage image = null;
		try {
			float magnification = 1.0f;
			Point maximum = transMeta.getMaximum();
			maximum.multiply(magnification);
			SwingGC gc = new SwingGC(null, maximum, 32, 0, 0);
			TransPainter transPainter = new TransPainter(gc, transMeta, maximum, null, null, null, null, null, new ArrayList<AreaOwner>(), new ArrayList<StepMeta>(), 32, 1, 0, 0,
					true, "Arial", 10);
			transPainter.setMagnification(magnification);
			transPainter.buildTransformationImage();
			image = (BufferedImage) gc.getImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public static Repository getRepository() {
		return App.getInstance().getRepository();
	}

}
