class RaceDashboardRouter extends Backbone.Router

  initialize: ()->
    @cars = new Backbone.Collection()
    car1 = new app.models.Car({name: "Car 1", speed: 0, dist: 0, lap: 0})
    @cars.add(car1)
    @trackView = new app.views.TrackView(model: @cars)
    @carSel = new app.views.TrackSelectorView(model: @cars)
    @rtDataView = new app.views.RTDataView(model: car1)
    @bind("rtevent", @newEvent, @)

  routes:
    "": "index"

  index: ()->
#    app.collections.Colls.fetch()
#    @collListView.addAll()

  newEvent: (event)->
    car = _.find(@cars.models,(each)-> event.car is each.get('name'))
    if car
      car.set(event.type, event.value)
    else
      newCar = new app.models.Car({name: event.car, speed: 0, dist: 0, lap: 0})
      newCar.set(event.type, event.value)
      @cars.add(newCar)


window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.RaceDashboardRouter=new RaceDashboardRouter()

$ ->
  Backbone.history.start
    pushHistory: true