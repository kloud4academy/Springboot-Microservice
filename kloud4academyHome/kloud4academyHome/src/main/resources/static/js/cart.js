jQuery(document).ready(function() {
    // This button will increment the value
    $('.qtyplus').click(function(e) {
      // Stop acting like a button
      e.preventDefault();
      // Get the field name
      fieldName = $(this).attr('field');
      // Get its current value
      var currentVal = parseInt($(this).siblings('input[name=' + fieldName + ']').val());
      // If is not undefined
      if (!isNaN(currentVal)) {
        // Increment
        $(this).siblings('input[name=' + fieldName + ']').val(currentVal + 1);
      } else {
        // Otherwise put a 0 there
        $(this).siblings('input[name=' + fieldName + ']').val(0);
      }
    });
    // This button will decrement the value till 0
    $(".qtyminus").click(function(e) {
      // Stop acting like a button
      e.preventDefault();
      // Get the field name
      fieldName = $(this).attr('field');
      // Get its current value
      var currentVal = parseInt($(this).siblings('input[name=' + fieldName + ']').val());
      // If it isn't undefined or its greater than 0
      if (!isNaN(currentVal) && currentVal > 0) {
        // Decrement one
        $(this).siblings('input[name=' + fieldName + ']').val(currentVal - 1);
      } else {
        // Otherwise put a 0 there
        $(this).siblings('input[name=' + fieldName + ']').val(0);
      }
    });
  });
$("#submitBtn").click(function(event) {
    event.preventDefault();
    fire_ajax_submit();
});
$('input[type=submit]').click(function() {
    $(this).attr('disabled', 'disabled');
    $(this).parents('form').submit();
});

function changeColor(selector, color) {
  //  $(selector).css('background-color', color);
    document.getElementById('colorInputBtn').value = color;
    event.preventDefault();

   $('form#myform .button').attr('onclick','').unbind('click');
  }
  function changeSize(selector, size) {
   // $(selector).css('background-color', size);
      document.getElementById('sizeInputBtn').value = size;
      event.preventDefault();
      $('form#myform .button').attr('onclick','').unbind('click');
    }
function fire_ajax_submit() {
    var cartData = {}
    cartData["size"] = $("#sizeInputBtn").val();
    cartData["color"] =  $("#colorInputBtn").val();
    cartData["quantity"] = $("#quantity").val();
	cartData["productId"] = $("#productIdBtn").val();
	cartData["price"] = $("#productPrice").val();
     $.ajax({
     			type: 'POST',
                url: '/productdetail/cart',
                contentType: 'application/json',
                crossDomain: false,
                data: JSON.stringify(cartData),
                async:true,
                success:function(data) {   
                  window.location.href = '/cartdetail/cart/'+data.body.msg;
                },
                error: function (e) {
                 $('#feedback').html(e.responseText);
            		console.log("ERROR : ", e);
        		}
            });

}
