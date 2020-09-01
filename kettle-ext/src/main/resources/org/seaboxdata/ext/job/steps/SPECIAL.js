JobEntrySpecial = Ext.extend(Ext.Window, {
	title: '作业定时调度',
	width: 400,
	height: 290,
	closeAction: 'close',
	modal: true,
	layout: 'fit',
	initComponent: function() {
		var me = this,
		graph = getActiveGraph().getGraph(), 
		cell = graph.getSelectionCell();
		
		var wRepeat = new Ext.form.Checkbox({fieldLabel: '重复', checked: cell.getAttribute('repeat') == 'Y'});

		var wIntervalMinutes = new Ext.form.NumberField({
			fieldLabel : '以分钟计算的间隔',
			flex : 1,
			//最大值
            maxValue: 999999999,
            //最小值
            minValue: 0,
            allowDecimals:false,//不允许输入小数bai
            //是否隐藏上下调节按钮
            hideTrigger: false,
            //键盘导航是否可用，启用后可以通过键盘的上下箭头调整数值
            keyNavEnabled: true,
            //鼠标滚轮是否可用，启用后可以通过滚动鼠标滚轮调整数值
            mouseWheelEnabled: true,
            grow : true,
            //通过调节按钮、键盘、鼠标滚轮调节数值时的大小
            step: 2,
			value : cell.getAttribute('intervalMinutes')
		});
		var wIntervalSeconds = new Ext.form.NumberField({
			fieldLabel : '以秒计算的间隔',
			flex : 1,
			//最大值
            maxValue: 999999999,
            //最小值
            minValue: 0,
            allowDecimals:false,//不允许输入小数bai
            //是否隐藏上下调节按钮
            hideTrigger: false,
            //键盘导航是否可用，启用后可以通过键盘的上下箭头调整数值
            keyNavEnabled: true,
            //鼠标滚轮是否可用，启用后可以通过滚动鼠标滚轮调整数值
            mouseWheelEnabled: true,
            //通过调节按钮、键盘、鼠标滚轮调节数值时的大小
            step: 2,
			value : cell.getAttribute('intervalSeconds')
		});
		
		var wHour =new Ext.form.NumberField( {
				name: 'hour',
				flex : 1,
				//最大值
	            maxValue: 23,
	            //最小值
	            minValue: 0,
	            allowDecimals:false,//不允许输入小数bai
	            //是否隐藏上下调节按钮
	            hideTrigger: false,
	            //键盘导航是否可用，启用后可以通过键盘的上下箭头调整数值
	            keyNavEnabled: true,
	            //鼠标滚轮是否可用，启用后可以通过滚动鼠标滚轮调整数值
	            mouseWheelEnabled: true,
	            //通过调节按钮、键盘、鼠标滚轮调节数值时的大小
	            step: 2,
				value: cell.getAttribute('hour')
			});
		var wMinutes=new Ext.form.NumberField( {
				name: 'minutes',
				flex : 1,
				//最大值
	            maxValue: 59,
	            //最小值
	            minValue: 0,
	            allowDecimals:false,//不允许输入小数bai
	            //是否隐藏上下调节按钮
	            hideTrigger: false,
	            //键盘导航是否可用，启用后可以通过键盘的上下箭头调整数值
	            keyNavEnabled: true,
	            //鼠标滚轮是否可用，启用后可以通过滚动鼠标滚轮调整数值
	            mouseWheelEnabled: true,
	            //通过调节按钮、键盘、鼠标滚轮调节数值时的大小
	            step: 2,
				value: cell.getAttribute('minutes')
			});
		 
		var wWeekDay=new Ext.form.ComboBox({
			fieldLabel: '每周',
			anchor: '-10',
			anchor: '-10',
			displayField: 'text',
			valueField: 'value',
			typeAhead: true,
	        mode: 'local',
	        forceSelection: true,
	        triggerAction: 'all',
	        selectOnFocus:true,
			store: new Ext.data.JsonStore({
	        	fields: ['value', 'text'],
	        	data: [{value: '1', text: '星期一'},
	        	       {value: '2', text: '星期二'},
	        	       {value: '3', text: '星期三'},
	        	       {value: '4', text: '星期四'},
	        	       {value: '5', text: '星期五'},
	        	       {value: '6', text: '星期六'},
	        	       {value: '7', text: '星期日'}]
		    }),
		    hiddenName: 'weekDay',
			value: cell.getAttribute('weekDay')
		}	);
		var wDayOfMonth=new Ext.form.NumberField({
			fieldLabel: '每月',
			anchor: '-10',
			name: 'dayOfMonth',
			flex : 1,
			//最大值
            maxValue: 30,
            //最小值
            minValue: 0,
            allowDecimals:false,//不允许输入小数bai
            //是否隐藏上下调节按钮
            hideTrigger: false,
            //键盘导航是否可用，启用后可以通过键盘的上下箭头调整数值
            keyNavEnabled: true,
            //鼠标滚轮是否可用，启用后可以通过滚动鼠标滚轮调整数值
            mouseWheelEnabled: true,
            //通过调节按钮、键盘、鼠标滚轮调节数值时的大小
            step: 2,
			value: cell.getAttribute('dayOfMonth')
		}	);
		
		var schedulerTypeVal = cell.getAttribute('schedulerType');
		if(schedulerTypeVal == 0){
    		wIntervalSeconds.setDisabled(true);
    		wIntervalMinutes.setDisabled(true);
    		wHour.setDisabled(true);
    		wMinutes.setDisabled(true);
    		wWeekDay.setDisabled(true);
    		wDayOfMonth.setDisabled(true);
    	} else if(schedulerTypeVal == 1) {
    		wIntervalSeconds.setDisabled(false);
    		wIntervalMinutes.setDisabled(false);
    		wHour.setDisabled(true);
    		wMinutes.setDisabled(true);
    		wWeekDay.setDisabled(true);
    		wDayOfMonth.setDisabled(true);
    	} else if(schedulerTypeVal == 2) {
    		wIntervalSeconds.setDisabled(true);
    		wIntervalMinutes.setDisabled(true);
    		wHour.setDisabled(false);
    		wMinutes.setDisabled(false);
    		wWeekDay.setDisabled(true);
    		wDayOfMonth.setDisabled(true);
    	} else if(schedulerTypeVal == 3) {
    		wIntervalSeconds.setDisabled(true);
    		wIntervalMinutes.setDisabled(true);
    		wHour.setDisabled(false);
    		wMinutes.setDisabled(false);
    		wWeekDay.setDisabled(false);
    		wDayOfMonth.setDisabled(true);
    	} else if(schedulerTypeVal == 4) {
    		wIntervalSeconds.setDisabled(true);
    		wIntervalMinutes.setDisabled(true);
    		wHour.setDisabled(false);
    		wMinutes.setDisabled(false);
    		wWeekDay.setDisabled(true);
    		wDayOfMonth.setDisabled(false);
    	}
		
		var wSchedulerType = new Ext.form.ComboBox({
			fieldLabel: '类型',
			anchor: '-10',
			displayField: 'text',
			valueField: 'value',
			typeAhead: true,
	        mode: 'local',
	        forceSelection: true,
	        triggerAction: 'all',
	        selectOnFocus:true,
			store: new Ext.data.JsonStore({
	        	fields: ['value', 'text'],
	        	data: [{value: '0', text: '不需要定时'},
	        	       {value: '1', text: '时间间隔'},
	        	       {value: '2', text: '天'},
	        	       {value: '3', text: '周'},
	        	       {value: '4', text: '月'}]
		    }),
		    hiddenName: 'schedulerType',
			value: cell.getAttribute('schedulerType'),
			listeners:{
	            //index是被选中的下拉项在整个列表中的下标 从0开始
	            'select':function(combo,record,index){
	            	if(combo.getValue() == 0){
	            		wIntervalSeconds.setDisabled(true);
	            		wIntervalMinutes.setDisabled(true);
	            		wHour.setDisabled(true);
	            		wMinutes.setDisabled(true);
	            		wWeekDay.setDisabled(true);
	            		wDayOfMonth.setDisabled(true);
	            	} else if(combo.getValue() == 1) {
	            		wIntervalSeconds.setDisabled(false);
	            		wIntervalMinutes.setDisabled(false);
	            		wHour.setDisabled(true);
	            		wMinutes.setDisabled(true);
	            		wWeekDay.setDisabled(true);
	            		wDayOfMonth.setDisabled(true);
	            	} else if(combo.getValue() == 2) {
	            		wIntervalSeconds.setDisabled(true);
	            		wIntervalMinutes.setDisabled(true);
	            		wHour.setDisabled(false);
	            		wMinutes.setDisabled(false);
	            		wWeekDay.setDisabled(true);
	            		wDayOfMonth.setDisabled(true);
	            	} else if(combo.getValue() == 3) {
	            		wIntervalSeconds.setDisabled(true);
	            		wIntervalMinutes.setDisabled(true);
	            		wHour.setDisabled(false);
	            		wMinutes.setDisabled(false);
	            		wWeekDay.setDisabled(false);
	            		wDayOfMonth.setDisabled(true);
	            	} else if(combo.getValue() == 4) {
	            		wIntervalSeconds.setDisabled(true);
	            		wIntervalMinutes.setDisabled(true);
	            		wHour.setDisabled(false);
	            		wMinutes.setDisabled(false);
	            		wWeekDay.setDisabled(true);
	            		wDayOfMonth.setDisabled(false);
	            	}
	            }
	        }
		});
		
		var form = new Ext.form.FormPanel({
			id: 'specialFormId',
			bodyStyle: 'padding: 15px',
			defaultType: 'textfield',
			labelWidth: 100,
			labelAlign: 'right',
			items: [wRepeat , wSchedulerType , wIntervalSeconds , wIntervalMinutes,{
				fieldLabel: '每天',
				xtype: 'compositefield',
				anchor: '-10',
				items: [wHour,wMinutes]
			},wWeekDay ,wDayOfMonth]
		});
		
		this.items = form;
		
		var bCancel = new Ext.Button({
			text: '取消', handler: function() {
				me.close();
			}
		});
		var bOk = new Ext.Button({
			text : '确定',
			scope : this,
			handler : function() {
				try {
					graph.getModel().beginUpdate();
					
					var data = {
						repeat : wRepeat.getValue() ? 'Y' : 'N',
						intervalMinutes : wIntervalMinutes.getValue(),
						intervalSeconds : wIntervalSeconds.getValue(),
						hour : wHour.getValue(),
						minutes : wMinutes.getValue(),
						weekDay : wWeekDay.getValue(),
						dayOfMonth : wDayOfMonth.getValue(),
						schedulerType : wSchedulerType.getValue()
					};
					try {
						for (var name in data) {
							var edit = new mxCellAttributeChange(cell, name, data[name]);
							graph.getModel().execute(edit);
						}
						this.fireEvent('save', this, data);
					} finally {
						graph.getModel().endUpdate();
					}
					me.close();
				} finally {
					graph.getModel().endUpdate();
				}

				me.close();
			}
		});

		this.bbar = ['->', bCancel, bOk];
		
		JobEntrySpecial.superclass.initComponent.call(this);
	}
});

Ext.reg('SPECIAL', JobEntrySpecial);
