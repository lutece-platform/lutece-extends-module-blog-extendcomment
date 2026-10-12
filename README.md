# Module blog-extendcomment

Back-office page that lists the comments of the blogs the current administrator edited, with the moderation
actions of module-extend-comment (publish, flag as important, pin, answer, remove).

## Rights

- `BLOG_EXTENDCOMMENT_MANAGEMENT` opens the page.
- The moderation actions are handled by module-extend-comment, which requires `RESOURCE_EXTENDER_MANAGEMENT`
  (plugin-extend): give both rights to the moderators.

## Requirements

- A `comment` extender declared on the `BLOG` resource type (on every blog, `*`, or on one blog).
- The blogs are found through the plugin-blog index: a blog appears once the blog indexer daemon has indexed it.
