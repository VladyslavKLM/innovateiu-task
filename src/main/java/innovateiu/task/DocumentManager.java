package innovateiu.task;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final List<Document> documentStorage = new ArrayList<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.id == null || document.id.isEmpty()) {
            document.setId(generateNewId());
            documentStorage.add(document);
            return document;
        }

        Optional<Document> existDocumentOptional = findById(document.getId());

        if (existDocumentOptional.isEmpty()) {
            documentStorage.add(document);
            return document;
        }

        Document existDocument = existDocumentOptional.get();
        existDocument.setTitle(document.getTitle());
        existDocument.setContent(document.getContent());
        existDocument.setAuthor(document.getAuthor());
        existDocument.setCreated(document.getCreated());

        return existDocument;
    }

    private boolean isDocumentWithIdExists(String id) {
        return documentStorage.stream()
                .anyMatch(doc -> doc.getId().equals(id));
    }

    private String generateNewId() {
        do {
            String id = UUID.randomUUID().toString();
            if (!isDocumentWithIdExists(id)) {
                return id;
            }
        } while (true);
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documentStorage.stream()
                .filter(document -> matchesCriteria(document, request))
                .collect(Collectors.toList());
    }

    private boolean matchesCriteria(Document document, SearchRequest request) {
        return (request.getTitlePrefixes() == null || request.getTitlePrefixes().isEmpty() ||
                request.getTitlePrefixes().stream().anyMatch(document.getTitle()::equalsIgnoreCase)) &&
                (request.getContainsContents() == null || request.getContainsContents().isEmpty() ||
                        request.getContainsContents().stream().anyMatch(document.getContent()::contains)) &&
                (request.getAuthorIds() == null || request.getAuthorIds().isEmpty() ||
                        request.getAuthorIds().contains(document.getAuthor().getId())) &&
                (request.getCreatedFrom() == null || document.getCreated().compareTo(request.getCreatedFrom()) >= 0) &&
                (request.getCreatedTo() == null || document.getCreated().compareTo(request.getCreatedTo()) <= 0);
    }


    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        return documentStorage.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst();
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
