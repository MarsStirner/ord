var datepicker_options_ru = {
	flat : false,
	starts : 1,
	prev : '&#9664;',
	next : '&#9654;',
	lastSel : false,
	mode : 'single',
	view : 'days',
	calendars : 1,
	format : 'Y-m-d',
	position : 'bottom',
	eventName : 'click',
	onRender : function() {
		return {};
	},
	onChange : function() {
		return true;
	},
	onShow : function() {
		return true;
	},
	onBeforeShow : function() {
		return true;
	},
	onHide : function() {
		return true;
	},
	locale : {
		days : [ "Воскресенье", "Понедельник", "Вторник", "Среда", "Четверг",
				"Пятница", "Суббота", "Воскресенье" ],
		daysShort : [ "Вск", "Пнд", "Втр", "Срд", "Чтв", "Птн", "Сбт", "Вск" ],
		daysMin : [ "Вс", "Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс" ],
		months : [ "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
				"Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь",
				"Декабрь" ],
		monthsShort : [ "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг",
				"Сен", "Окт", "Ноя", "Дек" ],
		weekMin : 'Нд'
	}
};