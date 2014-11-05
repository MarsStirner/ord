/**
 * Created by EUpatov on 05.11.2014.
 * Убирает выделение по двойному щелчку
 */
document.ondblclick = function DoubleClick(evt) {
    if (window.getSelection)
        window.getSelection().removeAllRanges();
    else if (document.selection)
        document.selection.empty();
};