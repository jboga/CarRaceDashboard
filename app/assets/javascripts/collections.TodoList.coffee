class TodoList extends Backbone.Collection

  model: app.models.Todo

  url: "/todos"

  completed: ()->
    @filter( (todo)->
      todo.get('completed')
    )

  remaining: ()->
    @without.apply(@, @completed())

  nextOrder: ()->
    if @length
      @last().get('order')+1
    else
      1

  comparator: (todo)->
    todo.get('order')

window.app=window.app || {}
window.app.collections=window.app.collections || {}
window.app.collections.Todos=new TodoList()