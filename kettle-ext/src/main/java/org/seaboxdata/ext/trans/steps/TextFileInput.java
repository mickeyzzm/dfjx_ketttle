package org.seaboxdata.ext.trans.steps;

import java.util.List;
import java.util.Locale;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.fileinput.BaseFileInputField;
import org.pentaho.di.trans.steps.fileinput.text.TextFileFilter;
import org.pentaho.di.trans.steps.fileinput.text.TextFileInputMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.seaboxdata.ext.core.PropsUI;
import org.seaboxdata.ext.trans.step.AbstractStep;
import org.seaboxdata.ext.utils.JSONArray;
import org.seaboxdata.ext.utils.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxUtils;


@Component("TextFileInput")
@Scope("prototype")
public class TextFileInput  extends AbstractStep {

	@Override
	public void decode(StepMetaInterface stepMetaInterface, mxCell cell, List<DatabaseMeta> databases, IMetaStore metaStore) throws Exception {
		TextFileInputMeta meta = (TextFileInputMeta) stepMetaInterface;
		
		meta.inputFiles.acceptingFilenames=("Y".equalsIgnoreCase(cell.getAttribute("acceptingFilenames")));
		meta.inputFiles.passingThruFields=("Y".equalsIgnoreCase(cell.getAttribute("passingThruFields;")));
		meta.inputFiles.acceptingField=(cell.getAttribute("acceptingField"));
		meta.inputFiles.acceptingStepName=(cell.getAttribute("acceptingStepName"));
		
		meta.content.fileType=cell.getAttribute("fileType");
		
		if (true) {//preview
			// mixed type for preview, for be able to eat any EOL chars
			meta.content.fileFormat = "mixed";
		} else {
			meta.content.fileFormat = cell.getAttribute("fileFormat");
		}
		
		meta.content.separator=cell.getAttribute("separator");
		meta.content.enclosure=cell.getAttribute("enclosure;");
		meta.content.escapeCharacter=cell.getAttribute("escapeCharacter");
		meta.content.rowLimit=(Const.toInt(cell.getAttribute("rowLimit"),0));
		meta.content.filenameField=(cell.getAttribute("filenameField"));
		meta.content.rowNumberField=(cell.getAttribute("rowNumberField"));
		meta.inputFiles.isaddresult=("Y".equalsIgnoreCase(cell.getAttribute("isaddresult")));
		
		meta.content.includeFilename=("Y".equalsIgnoreCase(cell.getAttribute("includeFilename")));
		meta.content.includeRowNumber=("Y".equalsIgnoreCase(cell.getAttribute("includeRowNumber;")));
		meta.content.rowNumberByFile=("Y".equalsIgnoreCase(cell.getAttribute("rowNumberByFile")));
		meta.content.header=("Y".equalsIgnoreCase(cell.getAttribute("header")));
		meta.content.nrHeaderLines=Const.toInt(cell.getAttribute("nrHeaderLines"), 1);
		meta.content.footer=("Y".equalsIgnoreCase(cell.getAttribute("footer")));
		meta.content.nrFooterLines=Const.toInt(cell.getAttribute("nrFooterLines"), 1);
		meta.content.lineWrapped=("Y".equalsIgnoreCase(cell.getAttribute("lineWrapped")));
		meta.content.nrWraps=Const.toInt(cell.getAttribute("nrWraps"), 1);
		meta.content.layoutPaged=("Y".equalsIgnoreCase(cell.getAttribute("layoutPaged")));
		meta.content.nrLinesPerPage=Const.toInt(cell.getAttribute("nrLinesPerPage"), 1);
		meta.content.nrLinesDocHeader=Const.toInt(cell.getAttribute("nrLinesDocHeader"), 1);
		meta.content.fileCompression=(cell.getAttribute("fileCompression"));
		meta.content.dateFormatLenient=("Y".equalsIgnoreCase(cell.getAttribute("dateFormatLenient")));
		meta.content.noEmptyLines=("Y".equalsIgnoreCase(cell.getAttribute("noEmptyLines")));
		meta.content.encoding=cell.getAttribute("encoding");
		//TODO
		//meta.content.length = wLength.getText();
		
		
		String fileName = cell.getAttribute("fileNameStore");
		JSONArray jsonArray = JSONArray.fromObject(fileName);
		
		String filter = cell.getAttribute("filterStore");
		JSONArray filterjsonArray = JSONArray.fromObject(filter);
		
		String fields = cell.getAttribute("fieldStore");
        JSONArray fieldsjsonArray = JSONArray.fromObject(fields);
		
        meta.allocate( jsonArray.size(), fieldsjsonArray.size(), filterjsonArray.size() );
        
		for(int i=0; i<jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			meta.inputFiles.realFileName[i] = jsonObject.optString("realFileName" );
			meta.inputFiles.fileName[i] = jsonObject.optString("fileName" );
			meta.inputFiles.fileMask[i] = jsonObject.optString("filemask") ;
			meta.inputFiles.excludeFileMask[i] = jsonObject.optString("excludeFileMask") ;
			meta.inputFiles.fileRequired[i] = jsonObject.optString("fileRequired")==null?"N": jsonObject.optString("fileRequired");
			meta.inputFiles.includeSubFolders[i] = jsonObject.optString("includeSubFolders"  )==null?"N":jsonObject.optString("includeSubFolders"  );
		}
		
		 BaseFileInputField[] filefields = new BaseFileInputField[fieldsjsonArray.size()];
			for(int i=0; i<fieldsjsonArray.size(); i++) {
				JSONObject jsonObject = fieldsjsonArray.getJSONObject(i);
				BaseFileInputField field = new BaseFileInputField();

				field.setName( jsonObject.optString("name" ) );
				field.setType( ValueMetaFactory.getIdForValueMeta(jsonObject.optString("type" )));
				field.setFormat( jsonObject.optString("format" ) );
				field.setPosition( Const.toInt(jsonObject.optString("position" ),-1) );
				field.setLength( Const.toInt(jsonObject.optString("length" ),-1) );
				field.setPrecision( Const.toInt(jsonObject.optString("precision" ),-1) );
				field.setCurrencySymbol( jsonObject.optString("currency" ));
				field.setDecimalSymbol( jsonObject.optString("decimal" ) );
				field.setGroupSymbol( jsonObject.optString("group" ));
				field.setNullString( jsonObject.optString("nullif" ));
				field.setIfNullValue( jsonObject.optString("ifnull" ));
				field.setTrimType( ValueMetaString.getTrimTypeByDesc(jsonObject.optString("trim_type" )));
				field.setRepeated( "Y".equalsIgnoreCase(jsonObject.optString("repeat" ) ));
				filefields[i] = field;
			}
			meta.inputFiles.inputFields=filefields;
		
		TextFileFilter[] filterFields = new TextFileFilter[filterjsonArray.size()];
		for(int i=0; i<filterjsonArray.size(); i++) {
			JSONObject jsonObject = filterjsonArray.getJSONObject(i);
			TextFileFilter filterField = new TextFileFilter();
			filterField.setFilterString( jsonObject.optString("filterString" ) );
			filterField.setFilterPosition( Const.toInt(jsonObject.optString("filterPosition" ),-1) );
			filterField.setFilterLastLine("Y".equalsIgnoreCase( jsonObject.optString("filterLastLine" ) ));
			filterField.setFilterPositive( "Y".equalsIgnoreCase(jsonObject.optString("filterPositive" ) ));
	        filterFields[i] = filterField;
		}
		meta.setFilter(filterFields);
		
		meta.errorHandling.errorIgnored=("Y".equalsIgnoreCase(cell.getAttribute("errorIgnored")));
		meta.errorHandling.skipBadFiles=("Y".equalsIgnoreCase(cell.getAttribute("skipBadFiles")));
		meta.errorHandling.fileErrorField=(cell.getAttribute("fileErrorField"));
		meta.errorHandling.fileErrorMessageField=(cell.getAttribute("fileErrorMessageField"));
		meta.setErrorLineSkipped("Y".equalsIgnoreCase(cell.getAttribute("errorLineSkipped")));
		meta.setErrorCountField(cell.getAttribute("errorCountField"));
		meta.setErrorFieldsField(cell.getAttribute("errorFieldsField"));
		meta.setErrorTextField(cell.getAttribute("errorTextField;"));
		
		meta.errorHandling.warningFilesDestinationDirectory=(cell.getAttribute("warningFilesDestinationDirectory"));
		meta.errorHandling.warningFilesExtension=(cell.getAttribute("warningFilesExtension"));
		meta.errorHandling.errorFilesDestinationDirectory=(cell.getAttribute("errorFilesDestinationDirectory"));
		meta.errorHandling.errorFilesExtension=(cell.getAttribute("errorFilesExtension"));
		meta.errorHandling.lineNumberFilesDestinationDirectory=cell.getAttribute("lineNumberFilesDestinationDirectory");
		meta.errorHandling.lineNumberFilesExtension=(cell.getAttribute("lineNumberFilesExtension;"));

		// Date format Locale
	    Locale locale = EnvUtil.createLocale(cell.getAttribute("dateFormatLocale"));
	    if ( !locale.equals( Locale.getDefault() ) ) {
	      meta.content.dateFormatLocale = locale;
	    } else {
	      meta.content.dateFormatLocale = Locale.getDefault();
	    }
		
		meta.content.breakInEnclosureAllowed=("Y".equalsIgnoreCase(cell.getAttribute("breakInEnclosureAllowed")));
		meta.content.filenameField=(cell.getAttribute("filenameField"));
		meta.content.rowNumberField=(cell.getAttribute("rowNumberField"));
		
		meta.additionalOutputFields.shortFilenameField=(cell.getAttribute("shortFileFieldName;"));
		meta.additionalOutputFields.pathField=(cell.getAttribute("pathFieldName"));
		meta.additionalOutputFields.hiddenField=(cell.getAttribute("hiddenFieldName"));
		meta.additionalOutputFields.lastModificationField=(cell.getAttribute("lastModificationTimeFieldName"));
		meta.additionalOutputFields.uriField=(cell.getAttribute("uriNameFieldName"));
		meta.additionalOutputFields.rootUriField=(cell.getAttribute("rootUriNameFieldName"));
		meta.additionalOutputFields.extensionField=((cell.getAttribute("extensionFieldName")));
		meta.additionalOutputFields.sizeField=(cell.getAttribute("sizeFieldName"));
	}

	@Override
	public Element encode(StepMetaInterface stepMetaInterface) throws Exception {
		TextFileInputMeta textFileInputMeta = (TextFileInputMeta) stepMetaInterface;
		Document doc = mxUtils.createDocument();
		Element e = doc.createElement(PropsUI.TRANS_STEP_NAME);
		
//		e.setAttribute("fileName", textFileInputMeta.inputFiles.fileName.toString());
//		e.setAttribute("filemask", textFileInputMeta.inputFiles.fileMask.toString());
//		e.setAttribute("excludeFileMask", textFileInputMeta.inputFiles.excludeFileMask.toString());
//		e.setAttribute("fileRequired", textFileInputMeta.inputFiles.fileRequired.toString());
//		e.setAttribute("includeSubFolders", textFileInputMeta.inputFiles.includeSubFolders.toString());
		e.setAttribute("fileType", 	textFileInputMeta.content.fileType );
		e.setAttribute("separator", textFileInputMeta.content.separator);
		e.setAttribute("enclosure", textFileInputMeta.content.enclosure);
		e.setAttribute("escapeCharacter",textFileInputMeta.content.escapeCharacter);
		e.setAttribute("breakInEnclosureAllowed", textFileInputMeta.content.breakInEnclosureAllowed ? "Y" : "N");
		e.setAttribute("header", textFileInputMeta.content.header ? "Y" : "N");
		e.setAttribute("nrHeaderLines",textFileInputMeta.content.nrHeaderLines +"");
		e.setAttribute("footer", textFileInputMeta.content.footer ? "Y" : "N");
		e.setAttribute("nrFooterLines", textFileInputMeta.content.nrFooterLines + "" );
		e.setAttribute("lineWrapped", textFileInputMeta.content.lineWrapped  ? "Y" : "N");
		e.setAttribute("nrWraps", textFileInputMeta.content.lineWrapped ? "Y" : "N");
		
		e.setAttribute("layoutPaged", textFileInputMeta.content.layoutPaged ? "Y" : "N");
		e.setAttribute("nrLinesDocHeader", textFileInputMeta.content.nrLinesDocHeader +"");
		e.setAttribute("nrLinesPerPage", textFileInputMeta.content.nrLinesPerPage + "");
		e.setAttribute("fileCompression", textFileInputMeta.content.fileCompression );
		e.setAttribute("noEmptyLines", textFileInputMeta.content.noEmptyLines ? "Y" : "N");
		e.setAttribute("includeFilename", textFileInputMeta.content.includeFilename ? "Y" : "N");
		e.setAttribute("filenameField", textFileInputMeta.content.filenameField );
		e.setAttribute("includeRowNumber", textFileInputMeta.content.includeRowNumber ? "Y" : "N");
		e.setAttribute("rowNumberByFile", textFileInputMeta.content.rowNumberByFile ? "Y" : "N");
		e.setAttribute("rowNumberField", textFileInputMeta.content.rowNumberByFile ? "Y" : "N");
		e.setAttribute("fileFormat", textFileInputMeta.content.fileFormat );
		e.setAttribute("rowLimit", textFileInputMeta.content.rowLimit +"");
//		e.setAttribute("TextFileInputField", textFileInputMeta.gette() + "");
		
//		e.setAttribute("filter", textFileInputMeta.getFilter() ? "Y" : "N");
		e.setAttribute("encoding", textFileInputMeta.getEncoding());
		e.setAttribute("errorIgnored", textFileInputMeta.errorHandling.errorIgnored ? "Y" : "N");
		e.setAttribute("errorCountField", textFileInputMeta.getErrorCountField());
		e.setAttribute("errorTextField", textFileInputMeta.getErrorTextField());
		e.setAttribute("warningFilesDestinationDirectory", textFileInputMeta.errorHandling.warningFilesDestinationDirectory );
		e.setAttribute("warningFilesExtension", textFileInputMeta.errorHandling.warningFilesExtension);
		e.setAttribute("errorFilesDestinationDirectory", textFileInputMeta.errorHandling.errorFilesDestinationDirectory);
		e.setAttribute("errorFilesExtension", textFileInputMeta.errorHandling.errorFilesExtension);
		e.setAttribute("lineNumberFilesDestinationDirectory", textFileInputMeta.errorHandling.lineNumberFilesDestinationDirectory);
		e.setAttribute("lineNumberFilesExtension", textFileInputMeta.errorHandling.lineNumberFilesExtension);
		e.setAttribute("dateFormatLenient", textFileInputMeta.content.dateFormatLenient ? "Y" : "N");
		e.setAttribute("dateFormatLocale", textFileInputMeta.content.dateFormatLocale.toString());
		e.setAttribute("errorLineSkipped", textFileInputMeta.isErrorLineSkipped() ? "Y" : "N");
		
		
		e.setAttribute("acceptingFilenames", textFileInputMeta.inputFiles.acceptingFilenames  ? "Y" : "N");
		e.setAttribute("passingThruFields", textFileInputMeta.inputFiles.passingThruFields  ? "Y" : "N");
		e.setAttribute("acceptingField", textFileInputMeta.inputFiles.acceptingField);
		e.setAttribute("acceptingStepName", textFileInputMeta.inputFiles.acceptingStepName);
		e.setAttribute("acceptingStep", textFileInputMeta.inputFiles.acceptingStepName );
		e.setAttribute("isaddresult", textFileInputMeta.inputFiles.isaddresult  ? "Y" : "N");
		e.setAttribute("shortFileFieldName", textFileInputMeta.additionalOutputFields.shortFilenameField);
		e.setAttribute("pathFieldName", textFileInputMeta.additionalOutputFields.pathField);
		
		e.setAttribute("hiddenFieldName", textFileInputMeta.additionalOutputFields.hiddenField);
		e.setAttribute("lastModificationTimeFieldName", textFileInputMeta.additionalOutputFields.lastModificationField);
		e.setAttribute("uriNameFieldName", textFileInputMeta.additionalOutputFields.uriField);
		e.setAttribute("rootUriNameFieldName", textFileInputMeta.additionalOutputFields.rootUriField);
		e.setAttribute("extensionFieldName", textFileInputMeta.additionalOutputFields.extensionField);
		
		//fileNameStore  meta.inputFiles
		JSONArray jsonArray1=new JSONArray();
        for(int i=0;i<textFileInputMeta.getFileName().length;i++){
            JSONObject json1=new JSONObject();
            json1.put("fileName",textFileInputMeta.inputFiles.fileName[i]);
            json1.put("realFileName",textFileInputMeta.inputFiles.realFileName[i]);
            json1.put("filemask", textFileInputMeta.inputFiles.fileMask[i]);
            json1.put("excludeFileMask", textFileInputMeta.inputFiles.excludeFileMask[i]);
            json1.put("fileRequired",textFileInputMeta.inputFiles.fileRequired[i]);
            json1.put("includeSubFolders",textFileInputMeta.inputFiles.includeSubFolders[i]);
            jsonArray1.add(json1);
        }
        e.setAttribute("fileNameStore", jsonArray1.toString());
		
		TextFileFilter[] filters = textFileInputMeta.getFilter();
		if(filters != null) {
			JSONArray filterjsonArray = new JSONArray();
			for(TextFileFilter filter : filters) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("filterString", filter.getFilterString());
				jsonObject.put("filterPosition", filter.getFilterPosition());
				jsonObject.put("filterLastLine", filter.isFilterLastLine());
				jsonObject.put("filterPositive", filter.isFilterPositive());
				
				filterjsonArray.add(jsonObject);
			}
			e.setAttribute("filterStore", filterjsonArray.toString());
		};

		BaseFileInputField[] inputFields = textFileInputMeta.inputFiles.inputFields;
		if(inputFields != null) {
			JSONArray inputFieldsjsonArray = new JSONArray();
			for(BaseFileInputField inputField : inputFields) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("name", inputField.getName());
				jsonObject.put("position", inputField.getPosition());
				jsonObject.put("type", inputField.getTypeDesc());
				jsonObject.put("format", inputField.getFormat());
				jsonObject.put("currencySymbol", inputField.getCurrencySymbol());
				jsonObject.put("decimalSymbol", inputField.getDecimalSymbol());
				jsonObject.put("groupSymbol", inputField.getGroupSymbol());
				jsonObject.put("nullString", inputField.getNullString());
				jsonObject.put("ifNullValue", inputField.getIfNullValue());
				jsonObject.put("trimtype", inputField.getTrimTypeDesc());
				jsonObject.put("repeat", inputField.isRepeated());
				if(inputField.getLength() != -1)
					jsonObject.put("length", inputField.getLength());
				if(inputField.getPrecision() != -1)
					jsonObject.put("precision", inputField.getPrecision());
				
				inputFieldsjsonArray.add(jsonObject);
			}
			e.setAttribute("fieldStore", inputFieldsjsonArray.toString());
		}
		
		return e;
	}

}
