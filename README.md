# MarqueeView
Scrolling marquee

## Features
- Supports multiple Marquee loops to scroll
- Use the SurfaceView for better performance
- Support TextureView
- Support to set the number of repeats
- Support FadingEdge
- Supports set initial offset ratio

![image](https://github.com/cyynf/MarqueeView/blob/master/image.gif)

## Usage

Add it in your root build.gradle at the end of repositories:
``` groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
Add the dependency
``` groovy
	implementation 'com.github.cyynf:MarqueeView:1.4'
```
Use SurfaceView
``` xml
<cpf.marqueeview.MarqueeView
        android:id="@+id/marquee_view"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        app:backgroundColor="@android:color/darker_gray"
        app:entries="@array/data"
        app:textColor="@android:color/white"
        app:textSize="16sp" />
```
Use TextureView
``` xml
<cpf.marqueeview.MarqueeTextureView
        android:id="@+id/marquee_view"
        android:layout_width="match_parent"
        android:layout_height="44dp"
        app:backgroundColor="@android:color/darker_gray"
        app:entries="@array/data"
        app:textColor="@android:color/white"
        app:textSize="16sp" />
```

## Attribute

| Attribute               | Description  | 
|:---				      |:---| 
| textSize                | Text size | 
| textColor               | Text color | 
| backgroundColor         | Background color |
| entries                 | Marquee array | 
| offset                  | The initial offset relative to the view width, range: [0,1] | 
| speed                   | The scrolling speed, suggest: [slow,middle,fast], range: (0,10] | 
| marqueeRepeatLimit      | Repeat the number |
| fadingEdge              | Marquee edge fade in and out |

## API

| API                       | Description  | 
|:---				        |:---| 
| start()                   | Start rolling, It needs to be called after modifying some Attribute | 
| stop()                    | Stop rolling | 
| setText()                 | Set Marquee array | 
| setOnItemClickListener()  | Marquee item click event | 