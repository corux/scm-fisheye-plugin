Ext.ns('corux.fisheye');

corux.fisheye.LinkWindow = Ext.extend(Ext.Window, {
  title : 'Link repositories wizard',
  initComponent : function() {
    this.addEvents('finish');
    var config = {
      title : this.title,
      layout : 'fit',
      width : 420,
      height : 190,
      closable : true,
      resizable : true,
      plain : true,
      border : false,
      modal : true,
      bodyCssClass : 'x-panel-mc',
      items : [ {
        id : 'fisheyeLinkRepositoriesWizard',
        xtype : 'fisheyeLinkRepositoriesWizard',
        listeners : {
          finish : {
            fn : this.onFinish,
            scope : this
          }
        }
      } ]
    };
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    corux.fisheye.LinkWindow.superclass.initComponent.apply(this, arguments)
  },

  onFinish : function(config) {
    this.fireEvent('finish', config);
    this.close()
  }
});

corux.fisheye.LinkRepositoriesWizard = Ext
    .extend(
        Ext.Panel,
        {
          titleText : 'Link repositories',
          okText : 'Ok',
          cancelText : 'Cancel',
          connectingText : 'Connecting',
          statusText : 'Status',
          failedText : 'Failed to retrieve fisheye repositories. Check your credentials.',
          successText : 'Repositories linked successfully.',
          waitMsgText : 'Sending data...',

          credentialsCardHelpText : 'Enter the login credentials for your fisheye instance. These credentials will be used to retrieve the list of available repositories from fisheye.',
          usernameText : 'Username',
          passwordText : 'Password',

          selectRepositoriesCardHelpText : 'Select which of the repositories should be updated to the found fisheye repositories. Existing links will be removed and replaced with the found values.',
          diffLabel : 'Diff: ',
          diffNoChangeLabel : 'no changes found',

          statusCardSuccess : '<b>Repositories were successfully updated</b>.',
          statusCardFailed : '<b>Failed to update repositories</b>',

          backText : 'Back',
          nextText : 'Next',
          closeBtnText : 'Close',

          initComponent : function() {
            this.addEvents('finish');
            var config = {
              layout : 'card',
              activeItem : 0,
              bbar : [ '->', {
                id : 'move-prev',
                text : this.backText,
                handler : this.navHandler.createDelegate(this, [ -1 ]),
                disabled : true,
                scope : this
              }, {
                id : 'move-next',
                text : this.nextText,
                handler : this.navHandler.createDelegate(this, [ 1 ]),
                disabled : false,
                scope : this
              }, {
                id : 'close-btn',
                text : this.closeBtnText,
                handler : this.applyChanges,
                disabled : true,
                scope : this
              } ],
              defaults : {
                autoScroll : true,
                listeners : {
                  clientvalidation : {
                    fn : this.formValidityMonitor,
                    scope : this
                  }
                }
              },
              items : [ {
                id : 'linkCredentialsCard',
                url : restUrl + 'plugins/fisheye/link/retrieve-mapping.json',
                xtype : 'form',
                monitorValid : true,
                defaultType : 'textfield',
                items : [ {
                  xtype : 'panel',
                  cls : 'x-form-item',
                  html : this.credentialsCardHelpText
                }, {
                  name : 'username',
                  fieldLabel : this.usernameText,
                  allowBlank : false
                }, {
                  name : 'password',
                  fieldLabel : this.passwordText,
                  inputType : 'password',
                  allowBlank : false
                } ]
              }, {
                id : 'linkSelectRepositoriesCard',
                url : restUrl + 'plugins/fisheye/link.json',
                xtype : 'form',
                monitorValid : true,
                defaultType : 'textfield',
                items : [ {
                  name : 'username',
                  xtype : 'hidden'
                }, {
                  name : 'password',
                  xtype : 'hidden'
                }, {
                  xtype : 'panel',
                  cls : 'x-form-item',
                  html : this.selectRepositoriesCardHelpText
                } ]
              }, {
                id : 'linkStatusCard',
                items : []
              } ]
            };

            Ext.apply(this, Ext.apply(this.initialConfig, config));
            corux.fisheye.LinkRepositoriesWizard.superclass.initComponent.apply(this, arguments);
          },

          formValidityMonitor : function(card, valid) {
            var activeCard = this.getLayout().activeItem;
            if (activeCard === card) {
              var nextButton = Ext.getCmp('move-next');
              if (valid && nextButton.disabled) {
                nextButton.setDisabled(false);
              } else if (!valid && !nextButton.disabled) {
                nextButton.setDisabled(true);
              }
            }
          },

          navHandler : function(direction) {
            var layout = this.getLayout();
            var nextItem = layout.activeItem.nextSibling();
            var prevItem = layout.activeItem.previousSibling();

            var nextId = null;
            if (direction === 1 && nextItem !== null) {
              nextId = nextItem.getId();
            } else if (direction === -1 && prevItem !== null) {
              nextId = prevItem.getId();
            }

            // do custom logic
            var currentId = layout.activeItem.getId();
            if (currentId === 'linkCredentialsCard' && nextId === 'linkSelectRepositoriesCard') {
              this.retrieveRepositoryMapping(layout.activeItem, nextItem);
            } else if (currentId === 'linkSelectRepositoriesCard' && nextId === 'linkStatusCard') {
              this.updateRepositoryMapping(layout.activeItem, nextItem);
            }

            // select next card
            if (nextId !== null) {
              layout.setActiveItem(nextId);
            }

            // set state for buttons
            nextItem = layout.activeItem.nextSibling();
            prevItem = layout.activeItem.previousSibling();
            var activeForm = layout.activeItem.form;
            var isFormValid = !activeForm || activeForm.isValid();
            Ext.getCmp('move-prev').setDisabled(prevItem === null);
            Ext.getCmp('move-next').setDisabled(!isFormValid || nextItem === null);
            Ext.getCmp('close-btn').setDisabled(nextItem !== null);
          },

          setRepositoryMappingInWizard : function(listCard, mapping) {
            if (debug) {
              console.debug(this.successText);
            }

            for (var i = 0; i < mapping.length; i++) {
              var item = mapping[i];
              item.diff = [];
              for (var j = 0; j < item.currentFisheyeRepositories.length; j++) {
                var currentRepo = item.currentFisheyeRepositories[j];
                if (item.newFisheyeRepositories.indexOf(currentRepo) === -1) {
                  item.diff.push('-' + currentRepo);
                }
              }
              for (var j = 0; j < item.newFisheyeRepositories.length; j++) {
                var newRepo = item.newFisheyeRepositories[j];
                if (item.currentFisheyeRepositories.indexOf(newRepo) === -1) {
                  item.diff.push('+' + newRepo);
                }
              }
            }

            mapping.sort(function(a, b) {
              if (a.diff.length !== b.diff.length && (a.diff.length === 0 || b.diff.length === 0)) {
                return b.diff.length - a.diff.length;
              }
              return a.repository.localeCompare(b.repository);
            });

            for (var i = 0; i < mapping.length; i++) {
              var item = mapping[i];

              var id = 'repo-' + item.repository;
              listCard.remove(id);
              listCard.add({
                xtype : 'checkbox',
                inputValue : item.repository,
                name : 'repositories',
                id : id,
                hideLabel: true,
                checked : item.diff.length !== 0 && item.newFisheyeRepositories.length > 0,
                disabled : item.diff.length === 0,
                boxLabel : '<b>' + item.repository + '</b>: ' + (item.diff.length > 0 ? this.diffLabel + item.diff : this.diffNoChangeLabel)
              });
            }
            listCard.doLayout();
          },

          setUpdateStatusInWizard : function(statusCard, action) {
            var html = this.statusCardSuccess;
            if (!action.response || action.response.status >= 400) {
              html = this.statusCardFailed;
            }

            var id = 'status';
            statusCard.remove(id);
            statusCard.add({
              xtype : 'panel',
              id : id,
              html : html,
              cls : 'x-form-item'
            });
            statusCard.doLayout();
          },

          retrieveRepositoryMapping : function(credentialsCard, listCard) {
            credentialsCard.getForm().submit({
              scope : this,
              method : 'POST',
              waitTitle : this.connectingText,
              waitMsg : this.waitMsgText,
              success : function(form, action) {
                this.setRepositoryMappingInWizard(listCard, action.result);
              },
              failure : function(form, action) {
                if (action.response.status === 200) {
                  this.setRepositoryMappingInWizard(listCard, action.result);
                  return;
                }
                this.navHandler(-1);
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

          updateRepositoryMapping : function(listCard, statusCard) {
            var form = listCard.getForm();
            // add username/password from credentials page
            form.setValues(listCard.previousSibling().getForm().getValues());

            form.submit({
              scope : this,
              method : 'POST',
              waitTitle : this.connectingText,
              waitMsg : this.waitMsgText,
              success : function(form, action) {
                this.setUpdateStatusInWizard(statusCard, action);
              },
              failure : function(form, action) {
                this.setUpdateStatusInWizard(statusCard, action);
              }
            });
          },

          applyChanges : function() {
            var panel = Ext.getCmp('repositories');
            if (panel) {
              panel.getGrid().reload();
            }
            this.fireEvent('finish');
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
          linkButtonText : 'Open wizard',

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
Ext.reg('fisheyeGlobalConfigPanel', corux.fisheye.GlobalConfigPanel);
Ext.reg('fisheyeLinkRepositoriesWizard', corux.fisheye.LinkRepositoriesWizard);

// register config panel
registerGeneralConfigPanel({
  id : 'fisheyeGlobalConfigPanel',
  xtype : 'fisheyeGlobalConfigPanel'
});
