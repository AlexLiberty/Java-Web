package itstep.learning.servlets;


public class CartServlet {
    
}
/*
АРІ авторизованого користувача на прикладі замовлень/кошику
1) Структура даних
[UserAccess]   [carts]             [cart_items]
id --------\     cart_id  ------\     cart_item_id
            \--- user_access_id  \--- cart_id  
                 created_at           product_id ------- [products]
                 closed_at            cart_item_price
                 is_cancelled         action_id --------- [actions]
                 cart_price           quantity
*/
