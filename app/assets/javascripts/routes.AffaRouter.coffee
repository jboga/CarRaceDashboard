class AffaRouter extends Backbone.Router

  initialize: ()->
    @productionView = new app.views.ProductionView()
    @nonFactureView = new app.views.NonFactureView()

  routes:
    "": "index"

  index: ()->
#    app.collections.Colls.fetch()
#    @collListView.addAll()


window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.AffaRouter=new AffaRouter()

$ ->
  Backbone.history.start
    pushHistory: true