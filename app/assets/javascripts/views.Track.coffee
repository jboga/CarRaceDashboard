class TrackView extends Backbone.View

  el: '#track'

  initialize: ()->
    center = new google.maps.LatLng(47.937504, 0.225700)
    mapOptions = {
      center: center
      disableDefaultUI: true
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    @theMap = new google.maps.Map($(@el)[0], mapOptions)
    kmlFile = "http://www.comintech.net/kml/LeMans.kml"
    @trackLayer = new google.maps.KmlLayer(kmlFile)
    @trackLayer.setMap(@theMap)
    @model.on('change:pos', @updatePos)


  updatePos: (car)=>
    theMarker = car.get('marker')
    pos = car.get('pos')
    carPos = new google.maps.LatLng(pos.latitude, pos.longitude)
    if theMarker
      theMarker.setPosition(carPos)
    else
      marker = new google.maps.Marker({map: @theMap, position: carPos, title: car.get('name')})
      car.set('marker', marker)
    console.log(car)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.TrackView = TrackView