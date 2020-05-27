FileExplorerWindow = Ext.extend(Ext.Window, {
	width: 400,
	height: 500,
	layout: 'border',
	modal: true,
	title: '文件浏览器',
	
	extension: 0,
	
	initComponent: function() {
		var me = this;
		var loader = new Ext.tree.TreeLoader({
			dataUrl: GetUrl('system/fileexplorer.do'),
		});
		var tree = new KettleTree({
			region: 'center',
			root: new Ext.tree.AsyncTreeNode({text: 'root'}),
			loader: loader,
			rootVisible: false,
			listeners : {
				'dblclick' : function(node, e) {
					if(node.leaf)
						me.fireEvent('ok', node.id);
				}
			}
		});

		var local_extension = this.extension;
		local_extension = (local_extension == 96  ? 64  : this.extension);//xlsx
		local_extension = (local_extension == 24  ?  8  : local_extension);//txt
		var store = new Ext.data.JsonStore({
			autoLoad : true,
			fields : [ 'type', 'desc' ],
			baseParams : {
				extension : this.extension
			},
			proxy : new Ext.data.HttpProxy({
				url : GetUrl('system/filextension.do'),
				method : 'POST'
			}),
			listeners : {
				load : function() {
					wExtension.setValue(local_extension);
				}
			} //在此加一个这个玩意，就可以了，呵呵，是在jsonstore中加的，注意
		});

		var wExtension = new Ext.form.ComboBox({
			flex: 1,
			displayField: 'desc',
			valueField: 'type',
			typeAhead: true,
	        forceSelection: true,
	        triggerAction: 'all',
	        selectOnFocus:true,
			store: store
		});
		
		var ok = function() {
			var node = tree.getSelectionModel().getSelectedNode();
			if(node)
				me.fireEvent('ok', node.id);
		};
		
		this.items = [tree, {
			region: 'south',
			height: 30,
			layout: 'hbox',
			bodyStyle: 'padding: 3px',
			items: [wExtension, {
				width: 8, border: false
			},{
				xtype: 'button', text: '取消', scope: this, handler: function() {this.close();}
			}, {
				width: 8, border: false
			},{
				xtype: 'button', text: '确定', scope: this, handler: ok
			}]
		}];
		
		loader.on('beforeload', function(l, node) {
			if(node == tree.getRootNode())
				loader.baseParams.path = '';
			else
				loader.baseParams.path = node.id;
			
			l.baseParams.extension = this.extension;
		}, this);
		
		FileExplorerWindow.superclass.initComponent.call(this);
		this.addEvents('ok');
		
		wExtension.on('select', function(cb, rec) {
			this.extension = rec.get('type');
			tree.getRootNode().removeAll(true);
			tree.getRootNode().reload();
		}, this);
		
	}
});