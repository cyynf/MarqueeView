# MarqueeView
Scrolling marquee

## Features:
- Supports multiple Marquee loops to scroll
- Use the SurfaceView for better performance
- Support to set the number of repeats
- Support FadingEdge
- Supports set initial offset ratio

## Usage
``` xml
<cpf.marqueeview.MarqueeView
        android:id="@+id/marquee_view"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        android:background="@android:color/darker_gray"
        app:entries="@array/data"
        app:textColor="@android:color/white"
        app:textSize="16sp" />
```

## Attribute

| Attribute               | Description  | 
|:---				      |:---| 
| textSize                | Text size | 
| textColor               | Text color | 
| entries                 | Marquee array | 
| offset                  | The initial offset relative to the view width, range: [0,1] | 
| speed                   | The scrolling speed, [slow,middle,fast], range: (0,~] | 
| marqueeRepeatLimit      | Repeat the number |
| fadingEdge              | Marquee edge fade in and out |

## API

| API                       | Description  | 
|:---				        |:---| 
| start()                   | Start rolling, It needs to be called after modifying some Attribute | 
| stop()                    | Stop rolling | 
| setText()                 | Set Marquee array | 
| setOnItemClickListener()  | Marquee item click event | 