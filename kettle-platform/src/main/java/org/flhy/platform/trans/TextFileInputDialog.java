package org.flhy.platform.trans;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.vfs2.FileObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.flhy.ext.PluginFactory;
import org.flhy.ext.base.GraphCodec;
import org.flhy.ext.utils.JSONArray;
import org.flhy.ext.utils.JSONObject;
import org.flhy.ext.utils.JsonUtils;
import org.flhy.ext.utils.StringEscapeHelper;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.compress.CompressionInputStream;
import org.pentaho.di.core.compress.CompressionProvider;
import org.pentaho.di.core.compress.CompressionProviderFactory;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.text.EncodingType;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputUtils;
import org.seaboxdata.systemmng.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/textFileInput")
public class TextFileInputDialog {
	public static final LoggingObjectInterface l = new SimpleLoggingObject("TextFileInputDialog", LoggingObjectType.TRANSMETA, null);
    
	@Autowired
    protected CommonService cService;

    /**
     * 获取输入输出字段
     *
     * @param stepName
     * @param graphXml
     * @param before   false回去输出字段，true获取输入字段
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/texFileInputFields")
    protected void texFileInputFields(@RequestParam String graphXml, @RequestParam String stepName, @RequestParam boolean before) throws Exception {
        stepName = StringEscapeHelper.decode(stepName);
        
        GraphCodec codec = (GraphCodec) PluginFactory.getBean(GraphCodec.TRANS_CODEC);
        TransMeta transMeta = (TransMeta) codec.decode(graphXml);

        StepMeta currentStepMeta = transMeta.findStep( stepName );
        
        TextFileInputMeta meta=(TextFileInputMeta)currentStepMeta.getStepMetaInterface();
        
        
        if( "CSV".equalsIgnoreCase(meta.content.fileType) ) {
        	getCSV( transMeta,  meta);
        	//getFieldsData(meta, true);
        } else {
        	//getFixed();
        }
    }
    
	public void getFieldsData(TextFileInputMeta in, boolean insertAtTop) {
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < in.inputFiles.inputFields.length; i++) {
			BaseFileInputField field = in.inputFiles.inputFields[i];
			//field.guess();
			JSONObject jsonObject = new JSONObject();

			jsonObject.put("name", Const.NVL(field.getName(), ""));

			String type = field.getTypeDesc();
			String format = field.getFormat();
			String position = "" + field.getPosition();
			String length = "" + field.getLength();
			String prec = "" + field.getPrecision();
			String curr = field.getCurrencySymbol();
			String group = field.getGroupSymbol();
			String decim = field.getDecimalSymbol();
			String def = field.getNullString();
			String ifNull = field.getIfNullValue();
			String trim = field.getTrimTypeDesc();
			String rep = field.isRepeated() ? "Y" : "N";

			if (type != null) {
				jsonObject.put("type", type);
			}
			if (format != null) {
				jsonObject.put("format", format);
			}
			if (position != null && !"-1".equals(position)) {
				jsonObject.put("position", position);
			}
			if (length != null && !"-1".equals(length)) {
				jsonObject.put("length", length);
			}
			if (prec != null && !"-1".equals(prec)) {
				jsonObject.put("precision", prec);
			}
			if (curr != null) {
				jsonObject.put("currency", curr);
			}
			if (decim != null) {
				jsonObject.put("decimal", decim);
			}
			if (group != null) {
				jsonObject.put("group", group);
			}
			if (def != null) {
				jsonObject.put("nullif", def);
			}
			if (ifNull != null) {
				jsonObject.put("ifnull", ifNull);
			}
			if (trim != null) {
				jsonObject.put("trim_type", trim);
			}
			if (rep != null) {
				jsonObject.put("repeat", rep);
			}

			jsonArray.add(jsonObject);
		}

	}
    
    /**
     * CSV
     * @throws Exception
     */
	public void getCSV(TransMeta transMeta, TextFileInputMeta meta) throws Exception {
		FileInputList textFileList = meta.getFileInputList(transMeta);
		InputStream fileInputStream;
		CompressionInputStream inputStream = null;
		StringBuilder lineStringBuilder = new StringBuilder(256);
		int fileFormatType = meta.getFileFormatTypeNr();

		String delimiter = transMeta.environmentSubstitute(meta.content.separator);
		String enclosure = transMeta.environmentSubstitute(meta.content.enclosure);
		String escapeCharacter = transMeta.environmentSubstitute(meta.content.escapeCharacter);

		if (textFileList.nrOfFiles() > 0) {
			try {
				LogChannel log = new LogChannel(meta);

				FileObject fileObject = textFileList.getFile(0);
				fileInputStream = KettleVFS.getInputStream(fileObject);

				CompressionProvider provider = CompressionProviderFactory.getInstance()
						.createCompressionProviderInstance(meta.content.fileCompression);
				inputStream = provider.createInputStream(fileInputStream);

				InputStreamReader reader;
				if (meta.getEncoding() != null && meta.getEncoding().length() > 0) {
					reader = new InputStreamReader(inputStream, meta.getEncoding());
				} else {
					reader = new InputStreamReader(inputStream);
				}

				EncodingType encodingType = EncodingType.guessEncodingType(reader.getEncoding());

				JSONArray jsonArray = new JSONArray();
				String line = TextFileInputUtils.getLine(log, reader, encodingType, fileFormatType, lineStringBuilder);
				if (line != null) {
					String[] fields = TextFileInputUtils.guessStringsFromLine(transMeta, log, line, meta, delimiter,
							enclosure, escapeCharacter);

					for (int i = 0; i < fields.length; i++) {
						JSONObject jsonObject = new JSONObject();
						String field = fields[i];
						if (field == null || field.length() == 0 || !meta.content.header) {
							field = "Field" + (i + 1);
						} else {
							// Trim the field
							field = Const.trim(field);
							// Replace all spaces & - with underscore _
							field = Const.replace(field, " ", "_");
							field = Const.replace(field, "-", "_");
						}

						jsonObject.put("name", field);
						jsonObject.put("type", "String");
						//jsonObject.put("length", field.length());

						jsonArray.add(jsonObject);
					}
				}

				JsonUtils.response(jsonArray);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (inputStream != null) {
						inputStream.close();
					}
				} catch (Exception e) {

				}
			}

		}
	}
}
