class RaceDashboardRouter extends Backbone.Router

  initialize: ()->
    @cars = new Backbone.Collection()
    @cars.comparator = (car)-> car.get('rank')
    car1 = new app.models.Car({name: "Car 1", rank: 0, iconUrl: "/assets/images/map-icons/Car 1.png", speed: 0, dist: 0, lap: 0, avgSpeed: 0, maxSpeed: 0})
    @cars.add(car1)
    @trackView = new app.views.TrackView(model: @cars)
    @carSel = new app.views.TrackSelectorView(model: @cars)
    @rtDataView = new app.views.RTDataView(model: car1)
    @statView = new app.views.StatDataView(model: @cars)
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
      @cars.sort()
    else
      newCar = new app.models.Car({
        name: event.car
        rank: 0
        iconUrl: "/assets/images/map-icons/" + event.car + ".png"
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