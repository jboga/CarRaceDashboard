class TestRouter extends Backbone.Router

  initialize: ()->
    @map = new app.views.Html5TrackView()

  routes:
    "": "index"

  index: ()->
#    Empty here

window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.TestRouter=new TestRouter()

$ ->
  Backbone.history.start
    pushHistory: true