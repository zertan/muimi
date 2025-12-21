// Compiled with the offline squint-cljs shim.

const categories = ["science", "tech", "politics", "philosophy", "business"];
function main() {
return (() => {
const root = sqeave.by_id("app");
const posts = blogPosts;
return sqeave.start_blog(root, posts, categories);
})();
}
main()
