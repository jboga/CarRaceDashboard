class FacetItemListView extends Backbone.View

  events:
    "click input[name='Tous']": 'clearAll'

  initialize: ()->
    @model.on('reset', @addAll)
    @model.on('change:isSelected', @clearTousCheck)
    @navigator = $(@el).find('div[name=navigator]')
    @navigator.html('')
    @whereString = new Backbone.Model where: ''
    @whereString.on('change:where', @refreshAll)

  render: ()->
    @addAll()

  addAll: ()=>
    @navigator.html('')
    @addOne item for item in @model.models

  addOne: (item)=>
    view=new window.app.views.FacetItemView(model:item)
    @navigator.append(view.render().el)

  clearAll: ()->
    @clear item for item in @model.models
    $(@el).find('input').attr('checked', false)
    $(@el).find('input[name=Tous]').attr('checked', true)
    @whereString.set 'where', @selectedItemsClause()

  clear: (item)->
    item.set 'isSelected', false, silent: true

  selectedItems: ()->
    @model.models.filter (item)-> item.get('isSelected')

  selectedItemsClause: ()->
    selection = @selectedItems()
    if selection.length is 0
      "all"
    else
      _.reduce(
        selection
        (str, item)=>
          str + '+' + item.get('name')
        ''
      )

  selectedItemsList: ()->
    _.map(
      @selectedItems()
      (item)=>
        item.get('name')
    )

  reselect: (oldSelection)->
    @reselectItem(item, oldSelection) for item in @model.models

  reselectItem: (item, oldSelection)->
    isSel = _.contains(oldSelection, item.get('name'))
    item.set('isSelected', isSel, silent: true)
    search = 'input[value=' + item.get('name') + ']'
    $(@el).find(search).attr('checked', isSel)

  clearTousCheck: ()=>
    if @whereString
      @whereString.set 'where', @selectedItemsClause()
    n = @selectedItems().length
    if n is 0
      $(@el).find('input[name=Tous]').attr('checked', true)
      @refreshAll()
    else
      $(@el).find('input[name=Tous]').attr('checked', false)

  getInfo: ()=>
    @info

  setInfo: (inf)=>
    @info = inf
    @

  setParentView: (view)=>
    @parentView = view
    @

  where: ()=>
    clause = @whereString.get 'where'
    if clause is ""
      clause = "all"
    clause

  refreshAll: ()=>
    @parentView.updateAllFrom(@)


window.app=window.app || {}
window.app.views=window.app.views || {}
window.app.views.FacetItemListView=FacetItemListView