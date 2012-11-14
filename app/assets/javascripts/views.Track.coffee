class TrackView extends Backbone.View

  el: '#track'

  initialize: ()->
    center = new google.maps.LatLng(47.937504, 0.225700)
    mapOptions = {
      center: center
#      disableDefaultUI: true
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    @theMap = new google.maps.Map($(@el)[0], mapOptions)
    kmlFile = "http://www.comintech.net/kml/LeMans.kml"
    @trackLayer = new google.maps.KmlLayer(kmlFile)
    @trackLayer.setMap(@theMap)
    @theMap.setZoom(10)
    @model.on('change:pos', @updatePos)


  updatePos: (car)=>
    theMarker = car.get('marker')
    pos = car.get('pos')
    carPos = new google.maps.LatLng(pos.latitude, pos.longitude)
    if theMarker
      theMarker.setPosition(carPos)
    else
      markerOptions = {
        map: @theMap
        icon: '/assets/images/map-icons/' + car.get('name') + '.png'
        position: carPos
        title: car.get('name')
      }
      marker = new google.maps.Marker(markerOptions)
      car.set('marker', marker)


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.TrackView = TrackView