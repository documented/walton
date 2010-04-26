$(document).ready(function() {
  $('.example').click(function() {
    $(this).find('.return').slideToggle(100, function() {
      // Animation complete
    });
  });
});

$(function() {
  SyntaxHighlighter.all();
  $(".toggle a").click(function(e) {
    $(this).parents(".toggle").children().toggle();
  });
});
