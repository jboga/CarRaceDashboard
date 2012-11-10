class NonFactureView extends Backbone.View
  el: "#nonaff"

  template: _.template($('#template-nonaff-pane').html())

#  initialize: ()->
#    @render()


  initialize: ()=>
    $(@el).html(@template())
    @views = new Backbone.Collection()
    @nacollListView=new app.views.FacetItemListView({el: '#coll-na-list', model: app.collections.NAColls}).setInfo('trig').setParentView(@)
    @naListView=new app.views.FacetItemListView({el: '#code-na-list', model: app.collections.Nonaff}).setInfo('code').setParentView(@)
    @views = [@naListView, @nacollListView]
    @graphView=new app.views.GraphView({id: 'nagraph'}).setDomain("na")
    app.collections.NAColls.fetch()
    app.collections.Nonaff.fetch()
    @


  updateAllFrom: (sourceView)=>
    trigs = @nacollListView.where()
    codes = @naListView.where()
    for view in @views
      do(view) =>
        if (view isnt sourceView)
          oldSel = view.selectedItemsList()
          collWhere = @nacollListView.where()
          affWhere = @naListView.where()
          view.model.url = "/infos?domain=na&info="+ view.getInfo() + "&trigs=" + collWhere + "&codes=" + affWhere
          view.model.fetch({success: ()=> view.reselect(oldSel)})
    @graphView.update(trigs, codes)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.NonFactureView = NonFactureView