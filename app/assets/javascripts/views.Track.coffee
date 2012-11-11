class TrackView extends Backbone.View

  el: '#track'

  initialize: ()->
    center = '47.937504,0.225700'
    path = ''
    if @model
      path = '&path=' + @model.path
    mapUrl = 'http://maps.googleapis.com/maps/api/staticmap?center='+center+path +
      '&zoom=13' +
      '&size=800x440&sensor=false&key=AIzaSyBN7xbPPncfSsHvn_e3YoLrL_bgebgg3x8'
    @img = $(@el).find('img[id=trackImg]')
    @img.attr('src', mapUrl)




window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.TrackView = TrackView