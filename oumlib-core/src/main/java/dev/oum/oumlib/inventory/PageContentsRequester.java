package dev.oum.oumlib.inventory;

import org.jspecify.annotations.NonNull;

@FunctionalInterface
public interface PageContentsRequester {

    @NonNull GuiPageContentsResult requestPageContents(int pageNumber, int startIndex, int endIndex);
}
