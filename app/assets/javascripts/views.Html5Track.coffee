class Html5TrackView extends Backbone.View

  el: '#track'
  widthM: 500
  heightM: 300


  initialize: ()->
    @stage = new Kinetic.Stage({
    container: 'track'
    width: @widthM
    height: @heightM
    })

    $.ajax(
      type: "GET",
      url: "/track",
      contentType: "application/json",
      data: ""
    ).done (result)=>
      @drawTrack(result)

    @model.on('change:pos', @updatePos)

  drawTrack: (result)=>
    mapImageObj = new Image()
    mapImageObj.onload = ()=>
      mapImage = new Kinetic.Image({
      image: mapImageObj
      width: @widthM
      height: @heightM
      })
      @layer.add(mapImage)
      mapImage.moveToBottom()
      @layer.draw()
    @layer = new Kinetic.Layer()
    @minTX = _.min(_.pluck(result, 'x'))
    @minTY = _.min(_.pluck(result, 'y'))
    @maxTX = _.max(_.pluck(result, 'x'))
    @maxTY = _.max(_.pluck(result, 'y'))

    mapurl = 'http://dev.virtualearth.net/REST/v1/Imagery/Map/Road?mapArea='+ @minTY + ',' + @minTX + ',' + @maxTY + ',' + @maxTX + '&mapSize='+@widthM+','+@heightM+'&format=png&mapMetadata=0&key=AvRShB8c6uie3nSjATiMunjWWCRCqyZR4cfukh-tPsLS0f6YlZF1HTaVH_tRQVko'
    mapdataurl = '/imageinfo?mapArea='+ @minTY + ',' + @minTX + ',' + @maxTY + ',' + @maxTX
    $.ajax(
      type: "GET",
      url:  mapdataurl,
      contentType: "application/json",
      data: ""
      async: false
    ).done (result)=>
      @calculateScale(result)

    mapImageObj.src = mapurl

    arr = _.map(result, (coord)=> [ @calcX(coord.x) , @calcY(coord.y)])
    track = _.flatten(arr)
    @poly = new Kinetic.Polygon({
    points: track,
    stroke: 'black',
    strokeWidth: 3
    })
    @layer.add(@poly)
    @stage.add(@layer)


  calculateScale: (result)=>
    @minY = result.resourceSets[0].resources[0].bbox[0]
    @minX = result.resourceSets[0].resources[0].bbox[1]
    @maxY = result.resourceSets[0].resources[0].bbox[2]
    @maxX = result.resourceSets[0].resources[0].bbox[3]
    @lonW = @maxX - @minX
    @latH = @maxY - @minY

  calcX: (lon)=>
    (lon - @minX) * @widthM / @lonW

  calcY: (lat)=>
    @heightM - ((lat - @minY) * @heightM / @latH)

  updatePos: (car)=>
    theMarker = car.get('marker')
    pos = car.get('pos')
    if theMarker
      theMarker.transitionTo({
      x: @calcX(pos.longitude)-15
      y: @calcY(pos.latitude)-35
      duration: 1
      })
    else
      imageObj = new Image()
      imageObj.onload = ()=>
        marker = new Kinetic.Image({
        x: @calcX(pos.longitude)-15
        y: @calcY(pos.latitude)-35
        image: imageObj
        })
        car.set('marker', marker)
        @layer.add(marker)
      imageObj.src = car.get('iconUrl')


window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.Html5TrackView = Html5TrackView