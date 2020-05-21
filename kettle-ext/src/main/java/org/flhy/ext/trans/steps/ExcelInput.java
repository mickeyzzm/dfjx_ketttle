package org.flhy.ext.trans.steps;

import java.util.List;

import org.flhy.ext.core.PropsUI;
import org.flhy.ext.trans.step.AbstractStep;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.metastore.api.IMetaStore;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by cRAZY on 2017/6/12.
 */
import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("ExcelInput")
@Scope("prototype")
public class ExcelInput extends AbstractStep{
    @Override
    public void decode(StepMetaInterface stepMetaInterface, mxCell cell, List<DatabaseMeta> databases, IMetaStore metaStore) throws Exception {
        ExcelInputMeta excel=(ExcelInputMeta)stepMetaInterface;
        String e=cell.getAttribute("noempty");
        
        excel.setRowLimit(Const.toLong(cell.getAttribute("limit"), 0L));
        excel.setEncoding(cell.getAttribute("encoding"));
        excel.setSpreadSheetType( SpreadSheetType.valueOf(cell.getAttribute("spreadsheet_type")) );

        excel.setFileField(cell.getAttribute("filefield"));
        excel.setSheetField(cell.getAttribute("sheetfield"));
        excel.setSheetRowNumberField(cell.getAttribute("sheetrownumfield"));
        excel.setRowNumberField(cell.getAttribute("rownumfield"));

        String addResultFile=cell.getAttribute("add_to_result_filenames");
        excel.setAddResultFile(null == addResultFile || "Y".equalsIgnoreCase(addResultFile) ? true : false);
        
        excel.setStartsWithHeader("Y".equalsIgnoreCase(cell.getAttribute("header")));
        excel.setIgnoreEmptyRows("Y".equalsIgnoreCase(e) || null == e);
        excel.setStopOnEmpty("Y".equalsIgnoreCase(cell.getAttribute("stoponempty")) || null == e);
        
        excel.setAcceptingFilenames("Y".equalsIgnoreCase(cell.getAttribute("accept_filenames")));
        excel.setAcceptingField(cell.getAttribute("accept_field"));
        excel.setAcceptingStepName(cell.getAttribute("accept_stepname"));
        //excel.searchInfoAndTargetSteps(steps);

        String file=cell.getAttribute("file");
        JSONArray fileArray= JSONArray.fromObject(file);

        String sheets=cell.getAttribute("sheets");
        JSONArray sheetsArray= JSONArray.fromObject(sheets);
        
        String fields=cell.getAttribute("fields");
        JSONArray fieldsArray= JSONArray.fromObject(fields);

        excel.allocate(fileArray.size(), sheetsArray.size(), fieldsArray.size());
        
        String[] fileName=new String[fileArray.size()];
        String[] fileMask=new String[fileArray.size()];
        String[] excludeFileMask=new String[fileArray.size()];
        String[] fileRequired=new String[fileArray.size()];
        String[] includeSubFolders=new String[fileArray.size()];
        for(int i=0;i<fileArray.size();i++){
            JSONObject jsonObject = fileArray.getJSONObject(i);
            fileName[i]=jsonObject.optString("name");
            fileMask[i]=jsonObject.optString("filemask");
            excludeFileMask[i]=jsonObject.optString("exclude_filemask");
            fileRequired[i]=jsonObject.optString("file_required");
            includeSubFolders[i]=jsonObject.optString("include_subfolders");
        }
        
        excel.setFileName(fileName);
        excel.setFileMask(fileMask);
        excel.setExcludeFileMask(excludeFileMask);
        excel.setFileRequired(fileRequired);
        excel.setIncludeSubFolders(includeSubFolders);

        for (int i = 0; i < sheetsArray.size(); i++) {
			JSONObject jsonObject = sheetsArray.getJSONObject(i);
			excel.getSheetName()[i] = jsonObject.getString("name");
			excel.getStartRow()[i] = Const.toInt(jsonObject.getString("startrow"), 0);
			excel.getStartColumn()[i] = Const.toInt(jsonObject.getString("startcol"), 0);
		}
        
        
        ExcelInputField[] inputFields=new ExcelInputField[fieldsArray.size()];
        for(int i=0;i<fieldsArray.size();i++){
            ExcelInputField field=new ExcelInputField();
            JSONObject jsonObject = fieldsArray.getJSONObject(i);
            field.setName(jsonObject.optString("name"));
            field.setType(jsonObject.optString("type"));
            
            String repeat=jsonObject.optString("repeat");
            field.setTrimType(ExcelOutput.getIndex(ExcelInputMeta.type_trim_code,jsonObject.optString("trim_type")));
            field.setRepeated(null==repeat?false:repeat.equalsIgnoreCase("Y"));
            
            field.setLength(Const.toInt(jsonObject.optString("length"), -1));
            field.setPrecision(Const.toInt(jsonObject.optString("precision"), -1));
           
            field.setFormat(jsonObject.optString("format"));
            field.setCurrencySymbol(jsonObject.optString("currency"));
            field.setDecimalSymbol(jsonObject.optString("decimal"));
            field.setGroupSymbol(jsonObject.optString("group"));

            inputFields[i]=field;
        }
        excel.setField(inputFields);

        // Error handling fields...
        String strictTypes = cell.getAttribute("strict_types");
		excel.setStrictTypes(null == strictTypes ? false : strictTypes.equalsIgnoreCase("Y"));
		 String error_ignored = cell.getAttribute("error_ignored");
        excel.setErrorIgnored(null == error_ignored ? false : error_ignored.equalsIgnoreCase("Y"));
        
        String error_line_skipped = cell.getAttribute("error_line_skipped");
        excel.setErrorLineSkipped(null == error_line_skipped ? false : error_line_skipped.equalsIgnoreCase("Y"));

        excel.setWarningFilesDestinationDirectory(cell.getAttribute("bad_line_files_destination_directory"));
        excel.setBadLineFilesExtension(cell.getAttribute("bad_line_files_extension"));
        excel.setErrorFilesDestinationDirectory(cell.getAttribute("error_line_files_destination_directory"));
        excel.setErrorFilesExtension( cell.getAttribute("error_line_files_extension"));
        excel.setLineNumberFilesDestinationDirectory(cell.getAttribute("line_number_files_destination_directory"));
        excel.setLineNumberFilesExtension(cell.getAttribute("line_number_files_extension"));
        
        excel.setShortFileNameField(cell.getAttribute("shortFileFieldName"));
        excel.setPathField(cell.getAttribute("pathFieldName"));
        excel.setIsHiddenField(cell.getAttribute("hiddenFieldName"));
        excel.setLastModificationDateField(cell.getAttribute("lastModificationTimeFieldName"));
        excel.setUriField(cell.getAttribute("uriNameFieldName"));
        excel.setRootUriField(cell.getAttribute("rootUriNameFieldName"));
        excel.setExtensionField(cell.getAttribute("extensionFieldName"));
        excel.setSizeField(cell.getAttribute("sizeFieldName"));
        
        for(int i=0;i<sheetsArray.size();i++){
            JSONObject jsonObject = sheetsArray.getJSONObject(i);
        }
    }

    @Override
    public Element encode(StepMetaInterface stepMetaInterface) throws Exception {
        ExcelInputMeta excel=(ExcelInputMeta)stepMetaInterface;
        Document doc = mxUtils.createDocument();
        Element e = doc.createElement(PropsUI.TRANS_STEP_NAME);

        e.setAttribute("header",excel.startsWithHeader()?"Y":"N");
        e.setAttribute("noempty",excel.ignoreEmptyRows()?"Y":"N");
        e.setAttribute("stoponempty",excel.stopOnEmpty()?"Y":"N");
        e.setAttribute("filefield",excel.getFileField());
        e.setAttribute("sheetfield",excel.getSheetField());
        e.setAttribute("sheetrownumfield",excel.getSheetRowNumberField());
        e.setAttribute("rownumfield",excel.getRowNumberField());
        e.setAttribute("limit",String.valueOf(excel.getRowLimit()));
        e.setAttribute("encoding",excel.getEncoding());
        e.setAttribute("add_to_result_filenames",excel.isAddResultFile()?"Y":"N");
        e.setAttribute("accept_filenames",excel.isAcceptingFilenames()?"Y":"N");
        e.setAttribute("accept_field",excel.getAcceptingField());
        e.setAttribute("accept_stepname",excel.getAcceptingStepName());

        JSONArray jsonArray1=new JSONArray();
        for(int i=0;i<excel.getFileName().length;i++){
            JSONObject json1=new JSONObject();
            json1.put("name",excel.getFileName()[i]);
            json1.put("filemask",excel.getFileMask()[i]);
            json1.put("exclude_filemask",excel.getExcludeFileMask()[i]);
            json1.put("file_required",excel.getFileRequired()[i]);
            json1.put("include_subfolders",excel.getIncludeSubFolders()[i]);
            jsonArray1.add(json1);
        }
        e.setAttribute("file",jsonArray1.toString());

        JSONArray jsonArray2=new JSONArray();
        for(int i=0;i<excel.getField().length;i++){
            ExcelInputField field=excel.getField()[i];
            JSONObject json2=new JSONObject();
            json2.put("name",field.getName());
            json2.put("type",field.getTypeDesc());
            json2.put("length",field.getLength());
            json2.put("precision",field.getPrecision());
            json2.put("trim_type",field.getTrimTypeCode());
            json2.put("repeat",field.isRepeated()?"Y":"N");
            json2.put("format",field.getFormat());
            json2.put("currency",field.getCurrencySymbol());
            json2.put("decimal",field.getDecimalSymbol());
            json2.put("group",field.getGroupSymbol());
            jsonArray2.add(json2);
        }
        e.setAttribute("fields",jsonArray2.toString());

        JSONArray jsonArray3=new JSONArray();
        for(int i=0;i<excel.getSheetName().length;i++){
            JSONObject json3=new JSONObject();
            json3.put("name",excel.getSheetName()[i]);
            json3.put("startrow",excel.getStartRow()[i]);
            json3.put("startcol",excel.getStartColumn()[i]);
            jsonArray3.add(json3);
        }
        e.setAttribute("sheets",jsonArray3.toString());
        
       e.setAttribute("strict_types", excel.isStrictTypes() ?"Y":"N");
       e.setAttribute("error_ignored",excel.isErrorIgnored()?"Y":"N");
       e.setAttribute("error_line_skipped", excel.isErrorLineSkipped()?"Y":"N");
       e.setAttribute("bad_line_files_destination_directory",excel.getWarningFilesDestinationDirectory());
       e.setAttribute("bad_line_files_extension", excel.getBadLineFilesExtension());
       e.setAttribute("error_line_files_destination_directory",excel.getErrorFilesDestinationDirectory());
       e.setAttribute("error_line_files_extension", excel.getErrorFilesExtension());
       e.setAttribute("line_number_files_destination_directory",excel.getLineNumberFilesDestinationDirectory());
       e.setAttribute("line_number_files_extension",excel.getLineNumberFilesExtension());
       e.setAttribute("shortFileFieldName",excel.getShortFileNameField());
       e.setAttribute("pathFieldName",excel.getPathField());
       e.setAttribute("hiddenFieldName",excel.isHiddenField());
       e.setAttribute("lastModificationTimeFieldName",excel.getLastModificationDateField());
       e.setAttribute("uriNameFieldName",excel.getUriField());
       e.setAttribute("rootUriNameFieldName",excel.getRootUriField());
       e.setAttribute("extensionFieldName",excel.getExtensionField());
       e.setAttribute("sizeFieldName",excel.getSizeField());
       e.setAttribute("spreadsheet_type", excel.getSpreadSheetType() != null ? excel.getSpreadSheetType().toString():"");
        return e;
    }
}
