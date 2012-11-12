class RaceDashboardRouter extends Backbone.Router

  initialize: ()->
    @trackView = new app.views.TrackView()
    @rtDataView = new app.views.RTDataView(model: new app.models.Car({speed: 0, distance: 0, lap: 0}))
    @bind("rtevent", @newEvent, @)

  routes:
    "": "index"

  index: ()->
#    app.collections.Colls.fetch()
#    @collListView.addAll()

  newEvent: (event)->
    @rtDataView.updateRTEvent(event)


window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.RaceDashboardRouter=new RaceDashboardRouter()

$ ->
  Backbone.history.start
    pushHistory: true