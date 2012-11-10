class ProductionView extends Backbone.View
  el: "#affect"

  template: _.template($('#template-aff-pane').html())

#  initialize: ()->
#    @render()


  initialize: ()=>
    $(@el).html(@template())
    @views = new Backbone.Collection()
    @collListView=new app.views.FacetItemListView({el: '#coll-list', model: app.collections.Colls}).setInfo('trig').setParentView(@)
    @affaireListView=new app.views.FacetItemListView({el: '#aff-list', model: app.collections.Affaires}).setInfo('code').setParentView(@)
    @clientListView=new app.views.FacetItemListView({el: '#client-list', model: app.collections.Clients}).setInfo('client').setParentView(@)
    @statsView = new app.views.StatsView()
    @views = [@clientListView, @affaireListView, @collListView]
    @graphView=new app.views.GraphView({id: 'graph'}).setDomain("affect")
    app.collections.Colls.fetch()
    app.collections.Affaires.fetch()
    app.collections.Clients.fetch()
    @

  updateAllFrom: (sourceView)=>
    trigs = @collListView.where()
    codes = @affaireListView.where()
    for view in @views
      do(view) =>
        if (view isnt sourceView)
          oldSel = view.selectedItemsList()
          collWhere = @collListView.where()
          affWhere = @affaireListView.where()
          view.model.url = "/infos?domain=affect&info="+ view.getInfo() + "&trigs=" + collWhere + "&codes=" + affWhere
          view.model.fetch({success: ()=> view.reselect(oldSel)})
    @graphView.update(trigs, codes)
    @statsView.update(trigs, codes)



window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.ProductionView = ProductionView