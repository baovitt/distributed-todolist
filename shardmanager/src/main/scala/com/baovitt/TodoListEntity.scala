package com.baovitt

// SHARDCAKE Imports:
import com.devsisters.shardcake.*

/** Enumeration of all modifications to a todo list in message form.
  */
enum TodoListMessage:
  /** Adds a new item to the todo list.
    *
    * @param description
    *   Description of the todo list item.
    * @param replier
    *   Replies with the id of the created item.
    */
  case AddItem(description: String, replier: Replier[Int])
      extends TodoListMessage

  /** Removes an item from the todo list.
    *
    * @param id
    *   Unique id for the todo list item to be removed.
    * @param replier
    *   Replies with if the item was removed.
    */
  case RemoveItem(id: Int, replier: Replier[Boolean]) extends TodoListMessage

  /** Changes the status of a todo list item to completed.
    *
    * @param id
    *   The id of the item to be modified.
    * @param replier
    *   Replies with if the item was modified.
    */
  case Completed(id: Int, replier: Replier[Boolean]) extends TodoListMessage

  /** Changes the status of a todo list item to incomplete.
    *
    * @param id
    *   The id of the item to be modified.
    * @param replier
    *   Replies with if the item was modified.
    */
  case Incompleted(id: Int, replier: Replier[Boolean]) extends TodoListMessage

  /** Changes the description of a todo list item.
    *
    * @param id
    *   The id of the item to be modified.
    * @param description
    *   The new description for the item.
    * @param replier
    *   Replies with if the item was modified.
    */
  case EditDescription(id: Int, description: String, replier: Replier[Boolean])
      extends TodoListMessage
end TodoListMessage

/** Todo list entity.
  */
object TodoListEntity extends EntityType[TodoListMessage]("todo_list"):

  // ZIO Imports:
  import zio.optics.*
  import zio.*

  /** Class representation of a todo list item.
    *
    * @param description
    *   Description of the todo list item.
    * @param completed
    *   Whether the todo list item has been completed.
    */
  final case class TodoListItem(description: String, completed: Boolean)

  /** Optics for the TodoListItem class.
    */
  object TodoListItem:

    /** Lens optic to modify the description of a todo list item.
      */
    lazy val description: Lens[TodoListItem, String] = Lens(
      item => Right(item.description),
      newDescription => item => Right(item.copy(description = newDescription))
    )

    /** Lens optic to modify the completion state of a todo list item.
      */
    lazy val completed: Lens[TodoListItem, Boolean] = Lens(
      item => Right(item.completed),
      newCompleted => item => Right(item.copy(completed = newCompleted))
    )
  end TodoListItem

  /** Entity handler to add an item to the todo list.
    *
    * @param state
    *   State of the current todo list.
    * @param message
    *   Todo list AddItem message.
    */
  def handleAddItem(
      state: Ref[Chunk[TodoListItem]],
      message: TodoListMessage.AddItem
  ): RIO[Sharding, Unit] =
    state
      .updateAndGet(_ :+ TodoListItem(message.description, false))
      .flatMap(items => message.replier.reply(items.length))

  /** Entity handler to remove an item to the todo list.
    *
    * @param state
    *   State of the current todo list.
    * @param message
    *   Todo list RemoveItem message.
    */
  def handleRemoveItem(
      state: Ref[Chunk[TodoListItem]],
      message: TodoListMessage.RemoveItem
  ): RIO[Sharding, Unit] =
    state
      .updateAndGet(_.patch(message.id, Nil, 1))
      .flatMap(items => message.replier.reply(items.length + 1 >= message.id))

  /** Entity handler to complete an item in the todo list.
    *
    * @param state
    *   State of the current todo list.
    * @param message
    *   Todo list Completed message.
    */
  def handleCompleted(
      state: Ref[Chunk[TodoListItem]],
      message: TodoListMessage.Completed
  ): RIO[Sharding, Unit] =
    state
      .updateAndGet(items =>
        (Optional.at(message.id)(items) >>> TodoListItem.completed)
          .set(true) getOrElse Chunk.empty
      )
      .flatMap(items => message.replier.reply(items.length > 0))

  /** Entity handler to incomplete an item in the todo list.
    *
    * @param state
    *   State of the current todo list.
    * @param message
    *   Todo list Incompleted message.
    */
  def handleIncompleted(
      state: Ref[Chunk[TodoListItem]],
      message: TodoListMessage.Incompleted
  ): RIO[Sharding, Unit] =
    state
      .updateAndGet(items =>
        (Optional.at(message.id)(items) >>> TodoListItem.completed)
          .set(false) getOrElse Chunk.empty
      )
      .flatMap(items => message.replier.reply(items.length > 0))

  /** Entity handler to change the description of an item in the todo list.
    *
    * @param state
    *   State of the current todo list.
    * @param message
    *   Todo list EditDescription message.
    */
  def handleEditDescription(
      state: Ref[Chunk[TodoListItem]],
      message: TodoListMessage.EditDescription
  ): RIO[Sharding, Unit] =
    state
      .updateAndGet(items =>
        (Optional.at(message.id)(items) >>> TodoListItem.description)
          .set(message.description) getOrElse Chunk.empty
      )
      .flatMap(items => message.replier.reply(items.length > 0))

  /** Defines the behavior of the todo_list entity.
    *
    * @param entityId
    *   Entity id.
    *
    * @param messages
    *   Queue of messages to the entity.
    */
  def behavior(
      entityId: String,
      messages: Dequeue[TodoListMessage]
  ): RIO[Sharding, Nothing] =
    Ref
      .make(Chunk.empty[TodoListItem])
      .flatMap(state =>
        messages.take.flatMap {
          case message: TodoListMessage.AddItem =>
            handleAddItem(state, message)
          case message: TodoListMessage.RemoveItem =>
            handleRemoveItem(state, message)
          case message: TodoListMessage.Completed =>
            handleCompleted(state, message)
          case message: TodoListMessage.Incompleted =>
            handleIncompleted(state, message)
          case message: TodoListMessage.EditDescription =>
            handleEditDescription(state, message)
        }.forever
      )
end TodoListEntity
