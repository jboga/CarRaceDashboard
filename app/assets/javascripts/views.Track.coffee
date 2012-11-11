class TrackView extends Backbone.View

  el: '#track'

  initialize: ()->
    center = new google.maps.LatLng(47.937504, 0.225700)
    mapOptions = {
      center: center
      disableDefaultUI: true
      mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    @map = new google.maps.Map($(@el)[0], mapOptions)
    kmlFile = "http://www.comintech.net/kml/LeMans.kml"
    @trackLayer = new google.maps.KmlLayer(kmlFile)
    @trackLayer.setMap(@map)



window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.TrackView = TrackView