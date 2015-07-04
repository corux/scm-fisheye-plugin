Ext.ns('corux.fisheye');

corux.fisheye.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  formTitleText : 'Fisheye',
  urlText : 'Url',
  apiTokenText : 'API Token',
  repositoriesText : 'Repositories',

  urlHelpText : 'Url of Fisheye installation (e.g. https://fisheye.yourserver.local/).',
  apiTokenHelpText : 'API token used to issue repository scan requests.',
  repositoriesHelpText : 'Comma separated list of fisheye repository names.',

  initComponent : function() {
    var config = {
      title : this.formTitleText,
      items : [ {
        name : 'fisheyeUrl',
        fieldLabel : this.urlText,
        property : 'fisheye.url',
        vtype : 'url',
        helpText : this.urlHelpText
      }, {
        name : 'fisheyeApiToken',
        fieldLabel : this.apiTokenText,
        property : 'fisheye.api-token',
        helpText : this.apiTokenHelpText
      }, {
        name : 'fisheyeRepositories',
        fieldLabel : this.repositoriesText,
        property : 'fisheye.repositories',
        helpText : this.repositoriesHelpText
      } ]
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    corux.fisheye.ConfigPanel.superclass.initComponent.apply(this, arguments);
  }
});

Ext.reg('fisheyeConfigPanel', corux.fisheye.ConfigPanel);

Sonia.repository.openListeners.push(function(repository, panels) {
  if (Sonia.repository.isOwner(repository)) {
    panels.push({
      xtype : 'fisheyeConfigPanel',
      item : repository
    });
  }
});
