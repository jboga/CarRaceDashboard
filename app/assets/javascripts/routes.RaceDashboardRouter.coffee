class RaceDashboardRouter extends Backbone.Router

  initialize: ()->
    @cars = new Backbone.Collection()
    @cars.comparator = (car)-> car.get('rank')
    @trackView = new app.views.Html5TrackView(model: @cars)
    @rtDataView = new app.views.RTDataView(model: @cars)
    @statView = new app.views.StatDataView(model: @cars)
    @bind("rtevent", @newEvent, @)

  routes:
    "": "index"

  index: ()->
#    Empty here

  newEvent: (event)->
    car = _.find(@cars.models,(each)-> event.car is each.get('name'))
    if car
      car.set(event.type, event.value)
      @cars.sort()
    else
      icon = "/assets/images/map-icons/Car" + (@cars.length + 1) + ".png"
      newCar = new app.models.Car({
        name: event.car
        rank: 0
        iconUrl: icon
        speed: 0
        dist: 0
        lap: 0
        avgSpeed: 0
        maxSpeed: 0
      })
      newCar.set(event.type, event.value)
      @cars.add(newCar)


window.app=window.app || {}
window.app.routers=window.app.routers || {}
window.app.routers.RaceDashboardRouter=new RaceDashboardRouter()

$ ->
  Backbone.history.start
    pushHistory: true