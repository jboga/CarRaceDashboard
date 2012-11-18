class AppView extends Backbone.View

  el: '#todoapp'

  statsTemplate: _.template( $('#stats-template').html())

  events:
    'keypress #new-todo': 'createOnEnter'
    'click #clear-completed': 'clearCompleted'
    'click #toggle-all': 'toggleAllComplete'

  initialize: ()->
    @input = $('#new-todo')
    @allCheckbox = $('#toggle-all')[0]

    app.collections.Todos.on( 'add', @addAll)
    app.collections.Todos.on( 'reset', @addAll)
    app.collections.Todos.on( 'change:completed', @addAll)
    app.collections.Todos.on( 'all', @render)

    @$footer = $('#footer')
    @$main = $('#main')

    app.collections.Todos.fetch()

  render: ()=>
    completed = app.collections.Todos.completed().length
    remaining = app.collections.Todos.remaining().length

    if app.collections.Todos.length
      @$main.show()
      @$footer.show();

      @$footer.html(@statsTemplate(
        completed: completed
        remaining: remaining
      ))

      $('#filters li a')
        .removeClass('selected')
        .filter('[href="#/' + ( app.TodoFilter || '' ) + '"]')
        .addClass('selected')
    else
      @$main.hide()
      @$footer.hide()

    @allCheckbox.checked = !remaining

  addOne: (todo)->
    view = new app.views.TodoView(
      model: todo
    )
    $('#todo-list').append( view.render().el )

  addAll: ()=>
    $('#todo-list').html('')

    switch( app.TodoFilter )
      when 'active' then @addOne todo for todo in app.collections.Todos.remaining()
      when 'completed' then @addOne todo for todo in app.collections.Todos.completed()
      else @addOne todo for todo in app.collections.Todos.models

  newAttributes: ()->
    title: @input.val().trim()
    order: app.collections.Todos.nextOrder()
    completed: false

  createOnEnter: (e)->
    if e.which is ENTER_KEY && @input.val().trim()?
      app.collections.Todos.create(@newAttributes())
      @input.val('')

  clearCompleted: ()=>
    todo.destroy() for todo in app.collections.Todos.completed()

  toggleAllComplete: ()->
    completed = @allCheckbox.checked
    todo.save(
      'completed': completed
    ) for todo in app.collections.Todos.models

window.app=window.app || {}
window.app.views=window.app.views || {}
window.app.views.AppView=AppView