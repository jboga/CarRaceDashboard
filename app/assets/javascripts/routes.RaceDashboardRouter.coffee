class RaceDashboardRouter extends Backbone.Router

  initialize: ()->
    @trackView = new app.views.TrackView()
    @rtDataView = new app.views.RTDataView()

  routes:
    "": "index"

  index: ()->
#    app.collections.Colls.fetch()
#    @collListView.addAll()


window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.RaceDashboardRouter=new RaceDashboardRouter()

$ ->
  Backbone.history.start
    pushHistory: true