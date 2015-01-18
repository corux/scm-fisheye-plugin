Ext.ns("corux.fisheye");

corux.fisheye.LinkWindow = Ext.extend(Ext.Window, {
  titleText : 'Link repositories',
  okText : 'Ok',
  cancelText : 'Cancel',
  connectingText : 'Connecting',
  statusText : 'Status',
  failedText : 'linking repositories failed!',
  successText : 'Repositories linked successfully.',
  waitMsgText : 'Sending data...',

  usernameText : 'Username',
  usernameHelpText : 'Username used to retrieve available repositories from the fisheye server.',

  passwordText : 'Password',
  passwordHelpText : 'Password used to retrieve available repositories from the fisheye server.',

  helpText : 'Retrieves all available repositories from fisheye and links them to the SCM repositories',

  initComponent : function() {
    var config = {
      layout : 'fit',
      width : 300,
      height : 170,
      closable : false,
      resizable : false,
      plain : true,
      border : false,
      modal : true,
      title : this.titleText,
      items : [ {
        id : 'linkRepositoryForm',
        url : restUrl + 'plugins/fisheye/link.json',
        frame : true,
        xtype : 'form',
        monitorValid : true,
        defaultType : 'textfield',
        items : [ {
          name : 'username',
          fieldLabel : this.usernameText,
          helpText : this.usernameHelpText,
          allowBlank : false
        }, {
          name : 'password',
          fieldLabel : this.passwordText,
          helpText : this.passwordHelpText,
          inputType : 'password',
          allowBlank : false
        }, {
          xtype : 'panel',
          html : this.helpText
        } ],
        buttons : [ {
          text : this.okText,
          formBind : true,
          scope : this,
          handler : this.linkRepositories
        }, {
          text : this.cancelText,
          scope : this,
          handler : this.cancel
        } ]
      } ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.action.ChangePasswordWindow.superclass.initComponent.apply(this, arguments);
  },

  linkRepositories : function() {
    var form = Ext.getCmp('linkRepositoryForm').getForm();
    form.submit({
      scope : this,
      method : 'POST',
      waitTitle : this.connectingText,
      waitMsg : this.waitMsgText,
      success : function() {
        if (debug) {
          console.debug(this.successText);
        }
        Ext.MessageBox.show({
          title : this.statusText,
          msg : this.successText,
          buttons : Ext.MessageBox.OK,
          icon : Ext.MessageBox.INFO
        });
        this.close();
      },

      failure : function() {
        if (debug) {
          console.debug(this.failedText);
        }
        Ext.MessageBox.show({
          title : this.statusText,
          msg : this.failedText,
          buttons : Ext.MessageBox.OK,
          icon : Ext.MessageBox.ERROR
        });
      }
    });
  },

  cancel : function() {
    this.close();
  }
});

corux.fisheye.GlobalConfigPanel = Ext
    .extend(
        Sonia.config.ConfigForm,
        {
          titleText : 'Fisheye Configuration',

          urlText : 'Url',
          urlHelpText : 'Url of Fisheye installation (e.g. https://fisheye.yourserver.local/).',

          useRepositoryNameAsDefaultText : 'Use repository name if not set',
          useRepositoryNameAsDefaultHelpText : 'Uses the SCM repository name as fisheye repository name, if no custom name is defined.',

          apiTokenText : 'API token',
          apiTokenHelpText : 'API token used to issue repository scan requests.',

          linkText : 'Link repositories',
          linkButtonText : 'Open configuration',
          linkHelpText : 'Retrieves all available repositories from fisheye and links them to the SCM repositories',

          initComponent : function() {

            var config = {
              title : this.titleText,
              items : [ {
                xtype : 'textfield',
                fieldLabel : this.urlText,
                name : 'url',
                vtype : 'url',
                allowBlank : true,
                helpText : this.urlHelpText
              }, {
                xtype : 'textfield',
                fieldLabel : this.apiTokenText,
                name : 'api-token',
                allowBlank : true,
                helpText : this.apiTokenHelpText
              }, {
                xtype : 'checkbox',
                fieldLabel : this.useRepositoryNameAsDefaultText,
                name : 'use-repository-name-as-default',
                inputValue : 'true',
                helpText : this.useRepositoryNameAsDefaultHelpText
              }, {
                xtype : 'button',
                fieldLabel : this.linkText,
                name : 'x-link-repositories',
                text : this.linkButtonText,
                helpText : this.linkHelpText,
                handler : function(btn) {
                  var win = new corux.fisheye.LinkWindow();
                  win.show();
                }
              } ]
            }

            Ext.apply(this, Ext.apply(this.initialConfig, config));
            corux.fisheye.GlobalConfigPanel.superclass.initComponent.apply(this, arguments);
          },

          onSubmit : function(values) {
            this.el.mask(this.submitText);
            Ext.Ajax.request({
              url : restUrl + 'plugins/fisheye/global-config.json',
              method : 'POST',
              jsonData : values,
              scope : this,
              disableCaching : true,
              success : function(response) {
                this.el.unmask();
              },
              failure : function() {
                this.el.unmask();
              }
            });
          },

          onLoad : function(el) {
            var tid = setTimeout(function() {
              el.mask(this.loadingText);
            }, 100);
            Ext.Ajax.request({
              url : restUrl + 'plugins/fisheye/global-config.json',
              method : 'GET',
              scope : this,
              disableCaching : true,
              success : function(response) {
                var obj = Ext.decode(response.responseText);
                this.load(obj);
                clearTimeout(tid);
                el.unmask();
              },
              failure : function() {
                el.unmask();
                clearTimeout(tid);
              }
            });
          }
        });

// register xtype
Ext.reg("fisheyeGlobalConfigPanel", corux.fisheye.GlobalConfigPanel);

// register config panel
registerGeneralConfigPanel({
  id : 'fisheyeGlobalConfigPanel',
  xtype : 'fisheyeGlobalConfigPanel'
});
