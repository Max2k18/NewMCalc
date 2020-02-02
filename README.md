# New MCalc
## A little about this repository
This is the repository of an application published on [Google Play](https://play.google.com/store/apps/details?id=com.maxsavteam.newmcalc).
I will try to add here a list of changes with each version. 
But I decided to conduct only from version 1.9.0, because I do not remember the changes of the previous ones. Sorry :)

### **Description of updates for the average user are published [here](https://newmcalc.maxsav.team/what-new/) in a more understandable language.**

#### **I waiting for feedback**

## Technical Change history
### 1.13.0
  * Added ceil and floor function
  * Added abs function
  * Core now run in a separate thread so that the main thread does not hang.
  * CoreConroller was added to control calculation time

### 1.12.3
  * Trigonometric functions now work with big numbers

### 1.12.2
  * Fixed trigonometric functions. Needed to convert degrees to radians and put radians value at parameter

### 1.12.0
  * Added side menu on main screen
  * New main activity: Main2Activity (MainActivity wil be deleted in after a while)
  * Special theme for Main2Activity, because it has own toolbar

### 1.11.6
  * Added support for the German language
  * New History Storage Protocol (HSP) v2
  * Added converter, which converts history from HSP v1 to v2

### 1.11.5
  * Clear History button moved to History activity
  * All broadcast receivers will be unregister on app pause and stop and then re-register (optimization)
  * The method of adding an expression from history has been simplified.
  * Added initialization objects to Fragment1 (FragmentOneInitializationObject),
                Fragment2( FragmentTwoInitializationObject)
                and MyFragmentPagerAdapter (FragmentAdapterInitializationObject)
  * All activities have been renamed

### 1.11.4
  * You can call value of entry, when last is in order
  * Force delete, when user long pressing the cancel button

### 1.11.3
  * Fixed mechanism for working with memory.

### 1.11.2
  * We returned the percentage function. And now it works in a completely different way.

### 1.11.1 (430)
From now on, all changes will be published on [this page](http://newmcalc.maxsav.team/what-new/).
  * Percent removed
  * Added the ability to delete variables.
  * After counting, you can immediately write the value to a variable.
  * Fixed strange counting behavior.
  * Fixed display of additional zeros after the fractional part of the application (they are no longer shown)
  * The buttons for selecting the length of the generated password are now square, the buttons for settings, history, etc. have a higher picture quality.
  
### 1.10.4.204
  * Error writing was fixed
  * EmptyStackException when calculating fixed
  

### 1.10.4.198
  * Now all calculation error notifications during calculations are displayed in short form on the main screen. (For this class CalculationError was added)

### 1.10.4.188
  * Errors during calculations are now displayed in the notification.
  * Bugs were fixed when calculating, for example, the logarithm of a negative number, etc.
  * Modularity++

### 1.10.3.171
  * Crash when calculating the percentage is accurately corrected.

### 1.10.3.170
  * Fixed a possible crash when calculating the percentage.
  * Fixed incorrect calculations of exponentiation.

### 1.10.3.165
  * Fixed strange behavior when deleting from history.
  * Fixed adding a "(" sign.

### 1.10.3.160
  * The bug related to adding a percent sign has been fixed.
  * Crash when deleting some records from the history is fixed.

### 1.10.3.B158
  * The Core has been moved from MainActivity to a separate package.
  * The number of decimal places has been changed from 8 to 15 decimal places.
  * Fixed the appearance of two buttons when canceling deletion from the history.

### 1.10.2.B154
  * Memory actions improved

### 1.10.1.B150
  * Icon for the converter has been added to the Shortcuts menu.

### 1.10.0.B142
  * Converter of number system was added
  * ActionBars were redrawn in light theme

### 1.9.2.B132
  * Arithmetic mean and geometric mean were added

### 1.9.0.B127
   * New navigation at main screen
