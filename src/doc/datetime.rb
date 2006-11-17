# Some nice routines from here:
# http://tom-eric.info/archive/2006/11/05/ruby-date-time

class Date
   def start_of_day
     DateTime.new(self.year,self.month,self.mday)
   end
 
   def end_of_day
     DateTime.new(self.year,self.month,self.mday,23,59,59)
   end
 
   def start_of_month
     DateTime.new(self.year,self.month,1)
   end
 
   def end_of_month
     year,month = self.month == 12 ? [self.year + 1, 1] : [self.year, self.month + 1]
 
     DateTime.new(year,month,1,23,59,59) - 1
   end
 
   def start_of_year
     DateTime.new(self.year,1,1)
   end
 
   def end_of_year
     DateTime.new(self.year,12,31,23,59,59)
   end
 
   def humanize
     current = Date.today
     case
     when self.year != current.year
       strftime("%Y-%m-%d")
     when self.yday == (current.yday + 1)
       "Tomorrow"
     when self.yday == current.yday
       "Today"
     when self.yday == (current.yday - 1)
       "Yesterday"
     else
       strftime("%d %b")
     end
   end
 end
 
 class DateTime
   def humanize
     current = Date.today
     case
     when self.year != current.year
       super
     when self.yday == current.yday
       strftime("%I:%M %p")
     else
       super + " at " + strftime("%I:%M %p")
     end
   end
 end
 
 class Time
   def to_datetime
     DateTime.new(self.year,self.month,self.mday,self.hour,self.min,self.sec)
   end
 
   def humanize
     self.to_datetime.humanize
   end
 end