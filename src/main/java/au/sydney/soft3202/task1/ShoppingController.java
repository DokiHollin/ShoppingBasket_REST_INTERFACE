package au.sydney.soft3202.task1;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import com.fasterxml.jackson.databind.node.DoubleNode;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

class Item extends RepresentationModel<Item>  {
    public Integer id;
    public String name;
    public String user;
    public int count;
    public Double cost;
    public Item(int id, String name, String user, int count, Double cost) {
           this.id = id;
           this.name = name;
           this.user = user;
           this.count = count;
           this.cost = cost;
    }
}


@RestController
@RequestMapping("/api/shop")
public class ShoppingController {

    private static Map<String, Double> costs = new HashMap<>();

    @Autowired
    private ShoppingBasketRepository shoppingBasket;

    @GetMapping("viewall")
    public List<Item> getBasket() {
//        System.out.println("this is getBasket method");
        List<ShoppingBasket> fs = shoppingBasket.findAll();
        List<Item> bs = new LinkedList<Item>();
        for (ShoppingBasket f : fs) {
            if (!costs.containsKey(f.getName())) {
                costs.put(f.getName(), 0.00);
            }
            bs.add(new Item(f.getId(), f.getName(), f.getUser(), f.getCount(), costs.get(f.getName())));
        }
        return bs;
    }

    // 1. View all users
    @GetMapping("/users")
    public List<String> getAllUsers() {
//        System.out.println("this is getAllUsers method");
        return shoppingBasket.findAll().stream().map(ShoppingBasket::getUser).distinct().collect(Collectors.toList());
    }

    // 2. View all costs
    @GetMapping("/costs")
    public Map<String, Double> getCosts() {
        //System.out.println("printing costs");
        return costs;
    }


    // 3. View the basket of a specific user
    @GetMapping("/users/{username}")
    public List<Map<String, Object>> getUserBasket(@PathVariable("username") String username) {
        //System.out.println("this is getUserBasket method");
        List<Map<String, Object>> basketList = new ArrayList<>();
        for (ShoppingBasket basket : shoppingBasket.findAll()) {
            if (basket.getUser().equals(username)) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", basket.getName());
                item.put("count", basket.getCount());
                basketList.add(item);
            }
        }
        return basketList;
    }

    // 4. View the total cost in the basket of a specific user
    @GetMapping("/users/{username}/total")
    public Double getTotalCostForUser(@PathVariable("username") String username) {
        //System.out.println("this is getTotalCostForUser method");
        double totalCost = 0.0;
        for (ShoppingBasket basket : shoppingBasket.findAll()) {
            if (basket.getUser().equals(username)) {
                totalCost += basket.getCount() * costs.getOrDefault(basket.getName(), 0.0);
            }
        }
        return totalCost;
    }

    // 5. Allow addition of the cost of a new item
    @PostMapping("/costs")
    public void addCost(@RequestBody Map<String, Object> itemCost) {
        //System.out.println("I put the: idk" + itemCost.toString());
        Double initCost = costs.get(itemCost.get("name").toString());
        if(initCost == null){
            initCost = 0.0;
        }
        double finalCost = initCost + Double.parseDouble(itemCost.get("cost").toString());
        costs.put(itemCost.get("name").toString(), finalCost);
    }



    // 6. Allow modification of the cost of an item
    @PutMapping("/costs/{item}")
    public void updateCost(@PathVariable("item") String item, @RequestBody Map<String, Object> cost) {
        //System.out.println("this is updateCost method"+Double.parseDouble(cost.get("cost").toString()));
        //System.out.println("item: "+item+"cost:");
//        if(!item.equals(cost.entrySet().toString())){
//            System.out.println("your path item name is: "+item+"which is different with requestBody item name: "+cost.entrySet().toString());
//        }
        costs.put(cost.get("name").toString(), Double.parseDouble(cost.get("cost").toString()));
    }

// 7. Add to the basket
    @PostMapping("/users/{username}/add")
    public void addToBasket(@PathVariable("username") String username, @RequestBody Map<String, Object> item) {
        //System.out.println("this is addToBasket method");
        //System.out.println("adding basket with name:"+username+" Item:"+item.toString());
        String itemName = (String) item.get("name");
        Integer count = Integer.parseInt(item.get("count").toString());

        ShoppingBasket basket = null;
        for (ShoppingBasket sb : shoppingBasket.findAll()) {
            if (sb.getUser().equals(username) && sb.getName().equals(itemName)) {
                basket = sb;
                break;
            }
        }

        if (basket == null) {
            // Create new basket for user
            basket = new ShoppingBasket();
            basket.setUser(username);
            basket.setName(itemName);
            basket.setCount(count);
        } else {
            // Increment the count
            basket.setCount(basket.getCount() + count);
        }
        shoppingBasket.save(basket);
    }

    // 8. Edit the basket
    @PutMapping("/users/{username}/basket/{item}")
    public void updateBasket(@PathVariable("username") String username, @PathVariable("item") String item,
                             @RequestBody Map<String, Object> countMap) {
        //System.out.println("this is updateBasket method");
        Integer count = Integer.parseInt(countMap.get("count").toString());
        ShoppingBasket basket = null;
        for (ShoppingBasket b : shoppingBasket.findAll()) {
            if (b.getUser().equals(username) && b.getName().equals(item)) {
                basket = b;
                break;
            }
        }
        if (basket == null) {
            System.out.println("User doesn't have this kind of item");
        }else{
            basket.setCount(count);
            shoppingBasket.save(basket);
        }

    }

    // 9. Allow deleting the basket of a user
    @DeleteMapping("/users/{username}")
    public void deleteBasket(@PathVariable("username") String username) {
        //System.out.println("this is deleteBasket method");
        List<ShoppingBasket> userBasket = new ArrayList<>();
        for (ShoppingBasket basket : shoppingBasket.findAll()) {
            if (basket.getUser().equals(username)) {
                userBasket.add(basket);
            }
        }
        shoppingBasket.deleteAll(userBasket);
    }

    // 10. Allow deleting an item in the basket
    @DeleteMapping("/users/{username}/basket/{item}")
    public void deleteItem(@PathVariable("username") String username, @PathVariable("item") String item) {
        //System.out.println("this is deleteItem method");
        ShoppingBasket basket = null;
        for (ShoppingBasket b : shoppingBasket.findAll()) {
            if (b.getUser().equals(username) && b.getName().equals(item)) {
                basket = b;
                break;
            }
        }
        if (basket == null) {
            System.out.println("User doesn't have this kind of item");
        }else{
            shoppingBasket.delete(basket);
        }

    }



}


