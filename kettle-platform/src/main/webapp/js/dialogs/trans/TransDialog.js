TransDialog = Ext.extend(Ext.Window, {
	title: '转换属性',
	width: 700,
	height: 500,
	closeAction: 'close',
	layout: 'fit',
	modal: true,
	bodyStyle: 'padding: 5px;',
	
	initComponent: function() {
		var me = this, graph = getActiveGraph().getGraph(), root = graph.getDefaultParent();
		
		var transForm = new TransTab();
		var transParam = new TransParamTab();
		/*var paramstore = new Ext.data.JsonStore({
			fields: ['name', 'default_value', 'description'],
		});
		var transParam = new Ext.grid.EditorGridPanel({
			title: '命名参数',
			tbar: [{
				text: '新增参数', handler: function() {
	                var rec = new paramstore.recordType({ value: '' });
	                transParam.stopEditing();
	                transParam.getStore().insert(0, rec);
	                transParam.startEditing(0, 0);
				}
			},{
				text: '删除参数', handler: function(btn) {
                    var sm = btn.findParentByType('editorgrid').getSelectionModel();
                    if(sm.hasSelection()) {
                        var row = sm.getSelectedCell()[0];
                        transParam.removeAt(row);
                    }
                }
			}],
			columns: [new Ext.grid.RowNumberer(), {
				header: '命名参数', dataIndex: 'name', width: 100, editor: new Ext.form.TextField({
		            allowBlank: false
		        })
			},{
				header: '默认值', dataIndex: 'default_value', width: 100, editor: new Ext.form.TextField({
		            allowBlank: false
		        })
			},{
				header: '描述', dataIndex: 'description', width: 100, editor: new Ext.form.TextField({
		            allowBlank: false
		        })
			}],
			store: paramstore
		});*/
		
		var transLog = new TransLogTab();
		var transDate = new TransDateTab();
		var transDependencies = new TransDependenciesTab();
		var transMisc = new TransMiscTab();
		var transMonitoring = new TransMonitoringTab();
		
		var tabPanel = new Ext.TabPanel({
			activeTab: 0,
			plain: true,
			items: [transForm, transParam, transLog, transDate, transDependencies, transMisc, transMonitoring]
		});
		
		this.items = tabPanel;
		this.bbar = [ '->', {
			text : '取消',
			handler : function() {
				me.close();
			}
		}, {
			text : '确定',
			handler : function() {
				graph.getModel().beginUpdate();
				try {
					var edit = new mxCellAttributeChange(cell, 'copies', textField.getValue());
					graph.getModel().execute(edit);
				} finally {
					graph.getModel().endUpdate();
				}

				me.close();
			}
		}
		];

		TransDialog.superclass.initComponent.call(this);
	}
});