Notes:

The `data` project has a service that will send a lot of events to the
`IngestServer` (once it is listening on the correct port). To run it:

- change to the `data` project (`project data`)
- `run`

You probably want to use two shells, one to run this service and one to run the
`IngestServer`.


Mission 0:

The `IngestServer` is listening on the wrong port. It needs to listen on 8808.


Mission 1:

Implement the `BookService`. This service stores all the books that are for
sale, which are sent via the `/books` end point. It needs:

- a method to set the store of books. A stub for this is already implemented as `addBooks`
- a method to return a book if one existings, given the name of a book. This
  will be used in validation.
  
  
Mission 2:

 Implement a `SessionService`. This service will store all the sessions (which
 are created when a `SessionStart` `Event` is received.) This service needs to
 be able to:
 
 - add a new session, when one is started
 - look a session by id.
 
 
 Mission 3:
 
 Start validating the other events. Some will have invalid sessions, or books,
 or other details. Try to catch as many as you can.
