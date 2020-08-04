package org.seaboxdata.platform.controller.trans;

import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.fileinput.FileInputList;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.spreadsheet.KCell;
import org.pentaho.di.core.spreadsheet.KCellType;
import org.pentaho.di.core.spreadsheet.KSheet;
import org.pentaho.di.core.spreadsheet.KWorkbook;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.WorkbookFactory;
import org.seaboxdata.ext.PluginFactory;
import org.seaboxdata.ext.base.GraphCodec;
import org.seaboxdata.ext.utils.JSONArray;
import org.seaboxdata.ext.utils.JSONObject;
import org.seaboxdata.ext.utils.JsonUtils;
import org.seaboxdata.ext.utils.StringEscapeHelper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/excelInput")
public class ExcelInputDialogController {

	/**
	 * 获取输入输出字段
	 *
	 * @param stepName
	 * @param graphXml
	 * @param before   false回去输出字段，true获取输入字段
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/excelInputFields")
	protected void excelInputFields(@RequestParam String graphXml, @RequestParam String stepName,
			@RequestParam boolean before) throws Exception {
		stepName = StringEscapeHelper.decode(stepName);

		GraphCodec codec = (GraphCodec) PluginFactory.getBean(GraphCodec.TRANS_CODEC);
		TransMeta transMeta = (TransMeta) codec.decode(graphXml);

		StepMeta currentStepMeta = transMeta.findStep(stepName);

		ExcelInputMeta info = (ExcelInputMeta) currentStepMeta.getStepMetaInterface();

		FileInputList fileList = info.getFileList(transMeta);

		RowMetaInterface rowMetaInterface = new RowMeta();

		for (FileObject file : fileList.getFiles()) {
			try {
				KWorkbook workbook = WorkbookFactory.getWorkbook(info.getSpreadSheetType(), KettleVFS.getFilename(file),
						info.getEncoding());
				processingWorkbook(rowMetaInterface, info, workbook);
				workbook.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < rowMetaInterface.size(); i++) {
			ValueMetaInterface v = rowMetaInterface.getValueMeta(i);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("name", v.getName());
			jsonObject.put("type", v.getTypeDesc());
			jsonObject.put("length", v.getLength() < 0 ? "-" : "" + v.getLength());
			jsonObject.put("precision", v.getPrecision() < 0 ? "-" : "" + v.getPrecision());
			jsonObject.put("origin", Const.NVL(v.getOrigin(), ""));
			jsonObject.put("storageType", ValueMeta.getStorageTypeCode(v.getStorageType()));
			jsonObject.put("conversionMask", Const.NVL(v.getConversionMask(), ""));
			jsonObject.put("currencySymbol", Const.NVL(v.getCurrencySymbol(), ""));
			jsonObject.put("decimalSymbol", Const.NVL(v.getDecimalSymbol(), ""));
			jsonObject.put("groupingSymbol", Const.NVL(v.getGroupingSymbol(), ""));
			jsonObject.put("trimType", ValueMeta.getTrimTypeDesc(v.getTrimType()));
			jsonObject.put("comments", Const.NVL(v.getComments(), ""));
			jsonArray.add(jsonObject);
		}
		JsonUtils.response(jsonArray);
	}

	public void processingWorkbook(RowMetaInterface fields, ExcelInputMeta info, KWorkbook workbook)
			throws KettlePluginException {
		int nrSheets = workbook.getNumberOfSheets();
		for (int j = 0; j < nrSheets; j++) {
			KSheet sheet = workbook.getSheet(j);

			// See if it's a selected sheet:
			int sheetIndex;
			if (info.readAllSheets()) {
				sheetIndex = 0;
			} else {
				sheetIndex = Const.indexOfString(sheet.getName(), info.getSheetName());
			}
			if (sheetIndex >= 0) {
				// We suppose it's the complete range we're looking for...
				//
				int rownr = 0;
				int startcol = 0;

				if (info.readAllSheets()) {
					if (info.getStartColumn().length == 1) {
						startcol = info.getStartColumn()[0];
					}
					if (info.getStartRow().length == 1) {
						rownr = info.getStartRow()[0];
					}
				} else {
					rownr = info.getStartRow()[sheetIndex];
					startcol = info.getStartColumn()[sheetIndex];
				}

				boolean stop = false;
				for (int colnr = startcol; !stop; colnr++) {
					try {
						String fieldname = null;
						int fieldtype = ValueMetaInterface.TYPE_NONE;

						KCell cell = sheet.getCell(colnr, rownr);
						if (cell == null) {
							stop = true;
						} else {
							if (cell.getType() != KCellType.EMPTY) {
								// We found a field.
								fieldname = cell.getContents();
							}

							// System.out.println("Fieldname = "+fieldname);

							KCell below = sheet.getCell(colnr, rownr + 1);

							if (below != null) {
								if (below.getType() == KCellType.BOOLEAN) {
									fieldtype = ValueMetaInterface.TYPE_BOOLEAN;
								} else if (below.getType() == KCellType.DATE) {
									fieldtype = ValueMetaInterface.TYPE_DATE;
								} else if (below.getType() == KCellType.LABEL) {
									fieldtype = ValueMetaInterface.TYPE_STRING;
								} else if (below.getType() == KCellType.NUMBER) {
									fieldtype = ValueMetaInterface.TYPE_NUMBER;
								} else {
									fieldtype = ValueMetaInterface.TYPE_STRING;
								}
							} else {
								fieldtype = ValueMetaInterface.TYPE_STRING;
							}

							if (Utils.isEmpty(fieldname)) {
								stop = true;
							} else {
								if (fieldtype != ValueMetaInterface.TYPE_NONE) {
									ValueMetaInterface field = ValueMetaFactory.createValueMeta(fieldname, fieldtype);
									fields.addValueMeta(field);
								}
							}
						}
					} catch (ArrayIndexOutOfBoundsException aioobe) {
						stop = true;
					}
				}
			}
		}
	}

}
