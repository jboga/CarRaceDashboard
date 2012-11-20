class RTDataView extends Backbone.View

  el: '#rtdata'
  speedGauge: null
  odo: null
  lapCounter: null
  driverSel: null
  lapDistance: 0
  driver: null

  events:
    "click a": 'changeSelection'
    "tap a": 'changeSelection'

  initialize: ()->
    @speedGauge = new jGauge()
    @speedGauge.id = 'speedGauge'
    @speedGauge.autoPrefix = autoPrefix.si
    @speedGauge.imagePath = 'assets/img/jgauge_face_taco.png'
    @speedGauge.segmentStart = -225
    @speedGauge.segmentEnd = 45
    @speedGauge.width = 170
    @speedGauge.height = 170
    @speedGauge.needle.imagePath = 'assets/img/jgauge_needle_taco.png'
    @speedGauge.needle.xOffset = 0
    @speedGauge.needle.yOffset = 0
    @speedGauge.label.yOffset = 80
    @speedGauge.label.color = '#fff'
    @speedGauge.label.precision = 0
    @speedGauge.label.suffix = 'km/h'
    @speedGauge.ticks.labelRadius = 45
    @speedGauge.ticks.labelColor = '#0ce'
    @speedGauge.ticks.start = 0
    @speedGauge.ticks.end = 300
    @speedGauge.ticks.count = 7
    @speedGauge.ticks.color = 'rgba(0, 0, 0, 0)'
    @speedGauge.range.color = 'rgba(0, 0, 0, 0)'
    @speedGauge.init()
    ctx = $('#distance')[0].getContext('2d')
    @odo = new odometer(ctx, {
    height: 40
    digits: 5
    decimals: 1
    value: 0
    wobbleFactor: 0.07
    })
    @lapCounter = new flipCounter('lapCounter', {value: 0})
    @lapDistance = trackLength
    @label = $(@el).find('button[id=driverSelLabel]')
    @driverSel = $(@el).find('ul[id=driverSel]')
    @model.on('add', @addAll)

  addAll: ()=>
    @driverSel.html('')
    @addOne driver for driver in @model.models

  addOne: (driver)=>
    @driverSel.append('<li><a href="#" name="' + driver.get('name') + '">' + driver.get('name') + '</a></li>')

  changeSelection: (event)->
    driverName = event.srcElement.name
    driver = _.find(@model.models, (aDrv)-> aDrv.get('name') is driverName)
    @changeSelectedDriver(driver)

  changeSelectedDriver: (selDriver)=>
    if @driver
      @driver.off('change', @updateRTData)
    @driver = selDriver
    @label.text(selDriver.get('name'))
    @updateRTData()
    @driver.on('change', @updateRTData)

  updateRTData: ()=>
    @speedGauge.setValue(@driver.get('speed'))
    @odo.setValue(@driver.get('dist'))
    lap = Math.floor(1 + @driver.get('dist') / @lapDistance)
    @lapCounter.setValue(lap)

window.app = window.app || {}
window.app.views = window.app.views || {}
window.app.views.RTDataView = RTDataView

