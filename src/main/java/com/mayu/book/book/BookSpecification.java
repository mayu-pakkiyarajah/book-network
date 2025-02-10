package com.mayu.book.book;

public class BookSpecification {

    public static Specification<Book> with OwnerId(Integer ownerId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"), ownerId)
    }
}
