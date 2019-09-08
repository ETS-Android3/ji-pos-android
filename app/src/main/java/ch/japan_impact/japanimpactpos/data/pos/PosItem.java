package ch.japan_impact.japanimpactpos.data.pos;

/**
 * @author Louis Vialar
 */
public class PosItem {
    private JIItem item;
    private int row;
    private int col;
    private String color;
    private String fontColor;

    public PosItem(JIItem item, int row, int col, String color, String fontColor) {
        this.item = item;
        this.row = row;
        this.col = col;
        this.color = color;
        this.fontColor = fontColor;
    }

    public PosItem() {
    }

    public JIItem getItem() {
        return item;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getColor() {
        return color;
    }

    public String getFontColor() {
        return fontColor;
    }

    @Override
    public String toString() {
        return "PosItem{" +
                "item=" + item +
                ", row=" + row +
                ", col=" + col +
                ", color='" + color + '\'' +
                ", fontColor='" + fontColor + '\'' +
                '}';
    }
}


/*
{"config":{"id":2,"name":"Caisses JI Test"},
"items":[
{"item":
    {"id":20,"name":"1 Jour Adulte","price":18,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},
    "row":0,
    "col":0,
    "color":"bg-danger",
    "fontColor":"text-white"
},{"item":{"id":21,"name":"1 Jour Etudiant","price":14,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":0,"col":1,"color":"bg-primary","fontColor":"text-white"},{"item":{"id":22,"name":"1 Jour 7-12 ans","price":8,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":0,"col":2,"color":"bg-secondary","fontColor":"text-white"},{"item":{"id":23,"name":"1 Jour -7 ans","price":0,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":0,"col":3,"color":"bg-success","fontColor":"text-white"},{"item":{"id":24,"name":"2 Jours Adulte","price":32,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":1,"col":0,"color":"bg-danger","fontColor":"text-white"},{"item":{"id":25,"name":"2 Jours Etudiant","price":24,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":1,"col":1,"color":"bg-primary","fontColor":"text-white"},{"item":{"id":26,"name":"2 Jours 7-12 ans","price":12,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":1,"col":2,"color":"bg-secondary","fontColor":"text-white"},{"item":{"id":27,"name":"2 Jours -7 ans","price":0,"description":"Test ticket","longDescription":"Test ticket","maxItems":-1,"eventId":1,"isTicket":true,"freePrice":false,"isVisible":false},"row":1,"col":3,"color":"bg-success","fontColor":"text-white"}]}
 */