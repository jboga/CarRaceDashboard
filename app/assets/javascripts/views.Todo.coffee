class TodoView extends Backbone.View

  tagName:  'li'

  template: _.template( $('#item-template').html() )

  events:
    'click .toggle':	'togglecompleted',
    'dblclick label':	'edit',
    'click .destroy':	'clear',
    'keypress .edit':	'updateOnEnter',
    'blur .edit':		'close'

  initialize: ()->
    @model.on( 'change', @render)
    @model.on( 'destroy', ()=>@remove())

  render: ()=>
    @$el.html( @template( @model.toJSON() ) )
    @$el.toggleClass( 'completed', @model.get('completed') )

    @input = @$('.edit')
    return @

  togglecompleted: ()->
    @model.toggle()

  edit: ()->
    @$el.addClass('editing')
    @input.focus()

  close: ()->
    value = @input.val().trim()
    if value?
      @model.save(
        title: value
      )
    else
      @clear()
    @$el.removeClass('editing')

  updateOnEnter: (e)->
    @close() if e.which is ENTER_KEY

  clear: ()->
    @model.destroy()

window.app=window.app || {}
window.app.views=window.app.views || {}
window.app.views.TodoView=TodoView
