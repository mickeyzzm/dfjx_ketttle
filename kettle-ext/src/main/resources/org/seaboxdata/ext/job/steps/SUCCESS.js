JobEntrySuccessDialog = Ext.extend(KettleDialog, {
	title: '结束',
	width: 300,
	height: 120,
	initComponent: function() {
		this.fitItems = [];
		JobEntrySuccessDialog.superclass.initComponent.call(this);
	}
});

Ext.reg('SUCCESS', JobEntrySuccessDialog);